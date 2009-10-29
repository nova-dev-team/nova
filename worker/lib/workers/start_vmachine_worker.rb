require "pp"

class StartVmachineWorker < BackgrounDRb::MetaWorker
  set_worker_name :start_vmachine_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  def start_vmachine args
    vm_name = args[:name]
    uuid = args[:uuid]
    resource_list = args[:resource_list]
    begin
      dom = Vmachine.find_domain_by_uuid uuid

      resource_list.each do |resource|
        if resource == args[:hda]
          ImageResource.prepare_vdisk vm_name, resource, "hda", uuid # might need to generate new qcow2 disks
        elsif resource == args[:hdb]
          ImageResource.prepare_vdisk vm_name, resource, "hdb", uuid
        else
          ImageResource.prepare_vdisk vm_name, resource
        end
      end

      Vmachine.log vm_name, "All resource prepared, creating vmachine domain"
      dom.create
      Vmachine.log vm_name, "Successfully created vmachine domain"
    rescue Exception => e
      Vmachine.log vm_name, "Create vmachine failed"
      Vmachine.log vm_name, e.to_s + "\n" + (e.backtrace.join "\n")
    end
  end
end

