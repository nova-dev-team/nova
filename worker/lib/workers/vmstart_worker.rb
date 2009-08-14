require 'libvirt'

class VmstartWorker < BackgrounDRb::MetaWorker
  
  
  @@virt_conn = Libvirt::open("qemu:///system")

  set_worker_name :vmstart_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  def do_start params
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
      # TODO setup status, copy files, then start vm
      #dom.create
    rescue
      # TODO report error by setting status
    end
  end

end

