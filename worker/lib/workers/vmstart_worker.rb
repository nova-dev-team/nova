require 'libvirt'

class VmstartWorker < BackgrounDRb::MetaWorker
  
  @@virt_conn = Libvirt::open("qemu:///system")

  set_worker_name :vmstart_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  def do_start uuid
    # TODO prepare disk images
    dom = @@virt_conn.lookup_domain_by_uuid uuid
    dom.create
  end
end

