module VmachineHelper

  require 'uri'
  require 'net/http'
  require 'uuidtools'
  require 'utils'

  include PmachineHelper

  class Helper

    def Helper.list
      list = []
      Vmachine.all.each {|vmachine| list << "v#{vmachine.id}"}
      return list
    end

    def Helper.create
      result = {}
      vmachine = Vmachine.create

      if vmachine.save # check save successful
        result[:success] = true
        result[:msg] = "Created vmachine v#{vmachine.id}"
        result[:vmachine_vid] = "v#{vmachine.id}"

      else # save failed
        result[:success] = false
        result[:msg] = "Failed to create new vmachine!"

      end

      return result
    end

    # delete a vmachine, this action will fail if the vmachine is under use
    def Helper.delete vmachine_vid
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found

        if vmachine.status != "not running" # check if under use
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is under use!"

        else # vmachine not under use, could be deleted
          Vmachine.delete vmachine
          result[:success] = true
          result[:msg] = "Removed vmachine #{vmachine_vid}"

        end
      end

      return result
    end

    def Helper.info vmachine_vid
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found
        result[:success] = true
        result[:vmachine_vid] = vmachine_vid
        result[:status] = vmachine.status

        if vmachine.vcluster != nil # if belongs to a vcluster
          result[:vcluster_cid] = "c#{vmachine.vcluster.id}"
        else
          result[:vcluster_cid] = ""
        end

        if vmachine.pmachine != nil # if hosted on a pmachine
          result[:pmachine_ip] = vmachine.pmachine.ip
        else
          result[:pmachine_ip] = ""
        end
      end

      return result
    end

    def Helper.start vmachine_vid
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found

        if vmachine.status != "not running" # not in correct status
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is already under use!"

        else # vmachine is "not running", so we could start it

          # dispatch the vmachine to a pmachine with lowest load
          hosting_pmachine = nil # find a pmachine to host the vmachine

          Pmachine.all.each do |pmachine|
            if pmachine.status == "pending remove"
              next
            end

            if hosting_pmachine == nil
              hosting_pmachine = pmachine

            elsif pmachine.vmachines.count < hosting_pmachine.vmachines.count
              hosting_pmachine = pmachine
              if hosting_pmachine.vmachines.count == 0
                # if the hosting pmachine has a load of 0 vmachines, we could break out of the loop now
                break
              end

            end
          end

          if hosting_pmachine == nil # pmachine not found
            result[:success] = false
            result[:msg] = "Cannot find a pmachine to host vmachine #{vmachine_vid}"

          else # pmachine found
            sub_result = PmachineHelper::Helper.host_vmachine hosting_pmachine.ip, "v#{vmachine.id}"

            if sub_result[:success] # successfully deployed pmachine
              
              kvm_xml = Utils::KvmXml.new
              kvm_xml.mem = 128  # TODO change parameters
              kvm_xml.ip = "10.0.3.1" # TODO allocate ip address
              kvm_xml.image = "os100m.img"  # TODO select image
              print kvm_xml.xml

url = URI.parse('http://' + hosting_pmachine.ip.to_s + ':3000/x/create')
req = Net::HTTP::Post.new(url.path)
req.set_form_data({:define => kvm_xml.xml })
res = Net::HTTP.new(url.host, url.port).start {
  |http| http.request(req)
}

case res
when Net::HTTPSuccess, Net::HTTPRedirection
  puts 'create vmachine ok'
else
  res.error!
end

              vmachine.status = "deploying"
              vmachine.pmachine = hosting_pmachine
              vmachine.save

              result[:pmon_info] = res.body

              result[:success] = true
              result[:xml] = kvm_xml.xml
              result[:msg] = "Deployed vmachine #{vmachine_vid} to pmachine #{hosting_pmachine.ip}"
# for demo purpose, immediately start pmachine
              res2 = Helper.notify_status_change vmachine_vid, "running"
              result[:msg] += "\n" + res2[:msg]

            else
              result[:success] = false
              result[:msg] = "Failed to host vmachine #{vmachine_vid} to pmachine #{hosting_pmachine.ip}: #{sub_result[:msg]}"

            end
          end

        end
      end

      return result
    end

    def Helper.stop vmachine_vid
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found

        if vmachine.status == "not running" # the vmachine is not running now
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is not running now!"

        elsif vmachine.status == "undeploying" # already in closing stage
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is already in closing stage!"

        else # could be closed, status = "deploying", "running", "suspended"
    
# TODO change uuid
         
          http_res = Net::HTTP.start(vmachine.pmachine.ip, 3000) do |http|
            http.put '/x/' + vmachine.pmon_vmachine_uuid + '/stop', ""
          end

          # TODO send control to pmon, change the vmachine into "undeploying" status
          vmachine.status = "undeploying"
          vmachine.pmon_vmachine_uuid = ""
          vmachine.save
          result[:success] = true
          result[:msg] = "Closing vmachine #{vmachine_vid}, 'undeploying' it now"

# XXX for demo purpose, notify status change immediately
          res2 = Helper.notify_status_change vmachine_vid, "not running"
          result[:msg] += "\n" + res2[:msg];

        end
      end

      return result
    end

    def Helper.suspend vmachine_vid
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found

        if vmachine.status != "running"
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is in status '#{vmachine.status}', cannot suspend!"

        else # status == running
          vmachine.status = "suspended"
          vmachine.save
          # TODO send suspend signal

# TODO change uuid
          http_res = Net::HTTP.start(vmachine.pmachine.ip, 3000) do |http|
            http.put '/x/' + vmachine.pmon_vmachine_uuid + '/suspend', ""
          end
          result[:success] = true
          result[:msg] = "Vmachine #{vmachine_vid} suspended"

        end
      end

      return result
    end

    def Helper.resume vmachine_vid
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found

        if vmachine.status != "suspended"
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is in status '#{vmachine.status}', cannot resume!"

        else # status == suspended
          vmachine.status = "running"
          vmachine.save
          # TODO send resume signal

# TODO change uuid
          http_res = Net::HTTP.start(vmachine.pmachine.ip, 3000) do |http|
            http.put '/x/' + vmachine.pmon_vmachine_uuid + '/resume', ""
          end
          result[:success] = true
          result[:msg] = "Vmachine #{vmachine_vid} resumed"

        end
      end

      return result
    end

    # NOTE only to be used for status change from "deploying" to "running", and "undeploying" to "not running"
    def Helper.notify_status_change vmachine_vid, new_status
      result = {}
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      else # vmachine found
        # check status
        if new_status == "running" and vmachine.status == "deploying"
          vmachine.status = "running"
          vmachine.save

          result[:success] = true
          result[:msg] = "Vmachine #{vmachine_vid} status changed from 'deploying' to 'running'"

        elsif new_status == "not running" and vmachine.status == "undeploying"
          vmachine.status = "not running"
          vmachine.pmachine = nil
          vmachine.save

          result[:success] = true
          result[:msg] = "Vmachine #{vmachine_vid} status changed from 'undeploying' to 'not running'"

        else # illegal status change
          result[:success] = false
          result[:msg] = "Illegal status change!"

        end
      end

      return result

    end
  
  end
end
