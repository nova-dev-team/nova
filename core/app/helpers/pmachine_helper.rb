module PmachineHelper

  class PmachineWorker

    # list all the pmachines' ip address
    def PmachineWorker.list
      list = []
      Pmachine.all.each {|pmachine| list << pmachine.ip}
      return list
    end

    # add a new pmachine
    def PmachineWorker.add pmachine_ip
      result = {}

      if pmachine_ip == nil # pmachine ip not provided
        result[:success] = false
        result[:msg] = "Pmachine ip not provided!"

      elsif Pmachine.find_by_ip(pmachine_ip) != nil # pamchine ip provided, but already added
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} already added!"

      else # pmachine ip provided and not added before
        # TODO authenticate the pmachine ip
        pmachine_ip_works = true

        if pmachine_ip_works
          pmachine = Pmachine.new(:ip => pmachine_ip)
          pmachine.save

          result[:success] = true
          result[:msg] = "Added new pmachine #{pmachine_ip}"

        else # not correct pmachine
          result[:success] = false
          result[:msg] = "Cannot authenticate ip address #{pmachine_ip}"

        end
      end

      return result

    end

    # mark a pmachine to be removed, so no new vmachine will be hosted on it
    def PmachineWorker.mark_remove pmachine_ip
      result = {}
      pmachine = Pmachine.find_by_ip pmachine_ip

      if pmachine == nil # pmachine not found
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} not found!"

      else # pmachine found

        if pmachine.vmachines.count == 0 # no vm is running, could remove the pmachine right now
          Pmachine.delete pmachine
          result[:success] = true
          result[:msg] = "Pmachine #{pmachine_ip} is immediately removed"

        else # the pmachine has some vm running, so we only markes it as "pending remove"
          pmachine.status = "pending remove"
          pmachine.save
          result[:success] = true
          result[:msg] = "Pmachine #{pmachine_ip} is marked pending remove"

        end
      end

      return result
    end

    # unmark a pmachine from "pending remove"
    def PmachineWorker.unmark_remove pmachine_ip
      result = {}
      pmachine = Pmachine.find_by_ip pmachine_ip

      if pmachine == nil # pmachine not found
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} not found!"

      else # pmachine found
        
        if pmachine.status != "pending remove" # not correct status
          result[:success] = false
          result[:msg] = "Pmachine #{pmachine_ip} is not marked 'pending remove'"

        else # has correct status "pending remove"
          pmachine.status = "working" # unmark pending delete
          pmachine.save

          result[:success] = true
          result[:msg] = "Pmachine #{pmachine_ip} unmarked from 'pending remove' status"
        end

      end

      return result
    end

    # host a vmachine on a pmachine, the pmachine must not be marked "pending remove"
    def PmachineWorker.host_vmachine pmachine_ip, vmachine_vid
      result = {}
      pmachine = Pmachine.find_by_ip pmachine_ip
      vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if pmachine == nil # pmachine not found
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} not found!"

      elsif vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vmachine #{vmachine_vid} not found!"

      elsif pmachine.status == "pending remove" # pmachine is to be removed, it cannot host anything
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} is pending remove!"

      else # both vmachine and pmachine are found, and pmachine is safe to host
        if vmachine.pmachine != nil # the vmachine is already hosted
          if vmachine.pmachine == pmachine # already hosted on 'this' pmachine
            result[:success] = false
            result[:msg] = "Vmachine #{vmachine_vid} is already hosted on pmachine #{pmachine_ip}"
            result[:s] = pmachine.status

          else # hosted on another pmachine
            result[:success] = false
            result[:msg] = "Vmachine #{vmachine_vid} is already hosted on other pmachine!"

          end

        else # vmachine is not hosted on any pmachine
          vmachine.pmachine = pmachine
          pmachine.save
          vmachine.save

          result[:success] = true
          result[:msg] = "Vmachine #{vmachine_vid} is hosted on pmachine #{pmachine.ip}"

        end
      end

      return result
    end

    # remove a vmachine from a pmachine (the vmachine must be 'not running' when doing this)
    def PmachineWorker.unhost_vmachine pmachine_ip, vmachine_vid
      result = {}
      pmachine = Pmachine.find_by_ip pmachine_ip

      if pmachine == nil # pmachine not found
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} not found!"

      else # pmachine found
        vmachine = pmachine.vmachines.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil
        
        if vmachine == nil # vmachine not found
          result[:success] = false
          result[:msg] = "Pmachine #{pmachine_ip} does not host vmachine #{vmachine_vid}"

        else # vmachine found
          if vmachine.status != "not running" # the vmachine must be "not running" when unhosting it
            result[:success] = false
            result[:msg] = "Vmachine #{vmachine_vid} must be 'not running' when unhosting it"

          else
            pmachine.vmachines.delete vmachine
            vmachine.pmachine = nil
            pmachine.save
            vmachine.save

            result[:success] = true
            result[:msg] = "Unhosted vmachine #{vmachine_vid} from pmachine #{pmachine_ip}"

          end
        end
      end

      return result
    end

    # show detailed info about a pmachine
    def PmachineWorker.info pmachine_ip
      result = {}
      pmachine = Pmachine.find_by_ip pmachine_ip

      if pmachine == nil # pmachine not found
        result[:success] = false
        result[:msg] = "Pmachine #{pmachine_ip} not found!"

      else # pmachine found
        result[:success] = true
        result[:pmachine_ip] = pmachine.ip
        result[:status] = pmachine.status

        hosted_vmachines = []
        pmachine.vmachines.each {|vmachine| hosted_vmachines << "v#{vmachine.id}"}
        result[:hosted_vmachines] = hosted_vmachines

      end
      return result
    end

  end

end
