class StartVmachineWorker < BackgrounDRb::MetaWorker
  set_worker_name :start_vmachine_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  def start_vmachine args
    uuid = args[:uuid]
    resource_list = args[:resource_list]
    # TODO
  end
end

