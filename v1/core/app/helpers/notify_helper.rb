module NotifyHelper
 
  include VmachineHelper # required for notify_status_change of vmachines
  
  class Helper

    def Helper.add uuid, receiver_type, receiver_id, type
      notify = Notify.new
      notify.notify_uuid = uuid
      notify.notify_receiver_type = receiver_type
      notify.notify_receiver_id = receiver_id
      notify.notify_type = type
      notify.save 
    end
    
    def Helper.notify uuid
      result = {}
      notify = Notify.find_by_uuid uuid
      if notify != nil # if found
        result[:success] = true
        # TODO execute the notify action
        if notify.notify_receiver_type == 'vmachine' # vmachine's notifications
          if notify.notify_type == 'deploying_finished' # status_change
            # TODO work on this crappy code
            VmachineHelper::Helper.notify_status_change notify.notify_receiver_id, 'running'
          elsif notify.notify_type == 'undeploying_finished' # status_change
            VmachineHelper::Helper.notify_status_change notify.notify_receiver_id, 'not running'
          end
        end

      else # notify not found
        result[:success] = false
        result[:msg] = "Notify with uuid #{uuid} not found!"
      end
      return result;
    end

  end

end
