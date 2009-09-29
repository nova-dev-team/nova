require "utils"
require "libvirt"

class Vmachine

  @@virt_conn = Libvirt::open("qemu:///system")

  def Vmachine.virt_conn
    @@virt_conn
  end

  def Vmachine.all_names
    all_domains.collect {|dom| dom.name}
  end

  def Vmachine.all_domains
    virt_conn = @@virt_conn

    all_domains = []
    # inactive domains are listed by name
    virt_conn.list_defined_domains.each do |dom_name|
      begin
        all_domains << virt_conn.lookup_domain_by_name(dom_name)
      rescue
        next # ignore error, go on with next one
      end
    end

    # active domains are listed by id
    virt_conn.list_domains.each do |dom_id|
      begin
        all_domains << virt_conn.lookup_domain_by_id(dom_id)
      rescue
        next
      end
    end
  end

  def Vmachine.find_domain uuid_or_name
    if uuid_or_name.is_uuid?
      uuid = uuid_or_name
      Vmachine.virt_conn.lookup_domain_by_uuid uuid
    else
      name = uuid_or_name
      Vmachine.virt_conn.lookup_domain_by_name name
    end
  end

  def Vmachine.define_domain xml_desc
    Vmachine.virt_conn.define_domain_xml xml_desc
  end

  def Vmachine.log vm_name, message
  end

  # non-blocking, most work is delegated to start_vmachine_worker
  def Vmachine.start params
    # TODO create a new domain

    begin
      xml_desc = Vmachine.emit_xml_desc params
      dom = Vmachine.defind_domain xml_desc
    rescue
      # check if the domain is already used
      begin
        Vmachine.find_domain params[:name]
        return {:success => false, :message => "Failed to create vmachine domain! Domain name '#{params[:name]}' already used!"}
      rescue
        # domain name not used, do nothing
      end
      return {:success => false, :message => "Failed to create vmachine domain!"}
    end

    begin
      MiddleMan.worker(:start_vmachine_worker).async_start_vmachine(dom.uuid)
      return {:success => true, :message => "Successfully created vmachine domain with name='#{dom.name}' and UUID=#{dom.uuid}. It is starting right now."}
    rescue
      return {:success => false, :message => "Failed to push 'start vmachine' request into job queue! Vmachine UUID=#{dom.uuid}."}
    end
  end

  # non-blocking, most work is delegated to stop_vmachine_worker
  def Vmachine.stop uuid
    # TODO stop a domain, and inform the update_vmachine_worker to upload it
  end

  # blocking method, will not take long time
  def Vmachine.suspend uuid
    Vmachine.libvirt_action "suspend", uuid
  end

  # blocking method
  def Vmachine.resume uuid
    Vmachine.libvirt_action "resume", uuid
  end

  # blocking method
  def Vmachine.destroy uuid
    Vmachine.libvirt_action "destroy", uuid
    # cleanup work is left for supervisor_worker, we just destroy the domain
  end

  def Vmachine.libvirt_action action_name, uuid
    begin
      dom = Vmachine.find_domain uuid
    rescue
      return {:success => false, :message => "Cannot find vmachine with UUID=#{uuid}!"}
    end

    begin
      dom.send action_name
      return {:success => true, :message => "Successfully processed action '#{action_name}' on vmachine with UUID=#{uuid}."}
    rescue
      return {:success => true, :message => "Failed to process action '#{action_name}' on vmachine with UUID=#{uuid}!"}
    end
  end
  
end

