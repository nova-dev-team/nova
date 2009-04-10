module VmachineHelper

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
        # TODO implement vm.start

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
              vmachine.status = "deploying"
              vmachine.save
              # TODO send control to pmon on corresponding pmachine

              result[:success] = true
              result[:msg] = "Deployed vmachine #{vmachine_vid} to pmachine #{hosting_pmachine.ip}"

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
          # TODO send control to pmon, change the vmachine into "undeploying" status
          vmachine.status = "undeploying"
          vmachine.save

          result[:success] = true
          result[:msg] = "Closing vmachine #{vmachine_vid}, 'undeploying' it now"

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
