require "utils"
require "libvirt"
require "fileutils"
require "uuidtools"

class Vmachine

  @@virt_conn = Libvirt::open("qemu:///system")

  def Vmachine.virt_conn
    @@virt_conn
  end

  def Vmachine.default_params
    {
      :arch => "i686",
      :emulator => "kvm", # ENHANCE currently we only support KVM
      :name => "dummy_vm",
      :vcpu => 1,
      :mem_size => 128,
      :uuid => UUIDTools::UUID.random_create.to_s,
      :hda => "",
      :hdb => "", # this is optional, could be "" (means no such a device)
      :cdrom => "", # this is optional, could be "" (means no such a device)
      :depend => "", # additional dependency on COW disks, separate with space
      :boot_dev => "hd", # hd, cdrom
      :vnc_port => -1,   # setting vnc_port to -1 means libvirt will automatically set the port
      :mac => ""  # mac is required, such as "11:22:33:44:55:66"
    }
  end

  def Vmachine.all_names
    all_domains.collect {|dom| dom.name}
  end

  def Vmachine.all_domains
    virt_conn = Vmachine.virt_conn

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

    return all_domains
  end

  def Vmachine.find_domain_by_uuid uuid
    Vmachine.virt_conn.lookup_domain_by_uuid uuid
  end

  def Vmachine.find_domain_by_name name
    Vmachine.virt_conn.lookup_domain_by_name name
  end

  def Vmachine.emit_domain_xml params
    if params[:cdrom] != nil and params[:cdrom] != ""
      FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
      cdrom_desc = <<CDROM_DESC
    <disk type='file' device='cdrom'>
      <source file='#{Setting.vmachines_root}/#{params[:name]}/#{params[:cdrom]}'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
CDROM_DESC
    end

    if params[:hda] != nil and params[:hda] != ""
      FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
      if VdiskNaming::vdisk_type(params[:hda]).start_with? "sys"
        real_hda_filename = "vd-notsaved-#{params[:uuid]}-hda.qcow2"
      else
        real_hda_filename = params[:hda]
      end
      hda_desc = <<HDA_DESC
    <disk type='file' device='disk'>
      <source file='#{Setting.vmachines_root}/#{params[:name]}/#{real_hda_filename}'/>
      <target dev='hda'/>
    </disk>
HDA_DESC
    end

    if params[:hdb] != nil and params[:hdb] != ""
      FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
      if VdiskNaming::vdisk_type(params[:hdb]).start_with? "system"
        real_hdb_filename = "vd-notsaved-hdb.qcow2"
      else
        real_hdb_filename = params[:hdb]
      end
      hdb_desc = <<HDB_DESC
    <disk type='file' device='disk'>
      <source file='#{Setting.vmachines_root}/#{params[:name]}/#{real_hdb_filename}'/>
      <target dev='hdb'/>
    </disk>
HDB_DESC
    end

    if params[:mac] != nil and params[:mac] != ""
      mac_desc = <<MAC_DESC
    <interface type='bridge'>
      <source bridge='br0'/>
      <mac address='#{params[:mac]}'/>
    </interface>
MAC_DESC
    end

# grpahics type=vnc port=-1: -1 means the system will automatically allocate an port for vnc
    xml_desc = <<XML_DESC
<domain type='qemu'>
  <name>#{params[:name]}</name>
  <uuid>#{params[:uuid]}</uuid>
  <memory>#{params[:mem_size].to_i * 1024}</memory>
  <vcpu>#{params[:vcpu]}</vcpu>
  <os>
    <type arch='#{params[:arch]}' machine='pc'>hvm</type>
    <boot dev='#{params[:boot_dev]}'/>
  </os>
  <features>
    <pae/>
    <acpi/>
  </features>
  <devices>
    <emulator>/usr/bin/kvm</emulator>
#{hda_desc if params[:hda] and params[:hda] != ""}
#{hdb_desc if params[:hdb] and params[:hdb] != ""}
#{cdrom_desc if params[:cdrom] and params[:cdrom] != ""}
#{mac_desc if params[:mac] and params[:mac] != ""}
    <graphics type='vnc' port='#{params[:vnc_port]}' listen='0.0.0.0'/>
    <input type='tablet' bus='usb'/>
  </devices>
</domain>
XML_DESC
  end

  def Vmachine.define_domain_xml xml_desc
    Vmachine.virt_conn.define_domain_xml xml_desc
  end

  def Vmachine.define params
    xml_desc = Vmachine.emit_domain_xml params
    puts xml_desc
    dom = Vmachine.define_domain_xml xml_desc
  end

  # write logs into vmachine folder
  def Vmachine.log vm_name, message
    vm_dir = File.join Setting.vmachines_root, vm_name
    FileUtils.mkdir_p vm_dir

    logfilename = File.join vm_dir, "log"
    logfile = File.open(logfilename, "a")
    logfile.flock(File::LOCK_EX) # lock the file, prevent possible race condition
    logfile.write("#{Time.now}: #{message}\n")
    logfile.close
  end

  # non-blocking, most work is delegated to start_vmachine_worker
  def Vmachine.start params
    # create a new domain
    begin
      dom = Vmachine.define params
      # remove any possible existing files
      FileUtils.rm_rf "#{Setting.vmachines_root}/#{params[:name]}"
      Vmachine.log params[:name], "Defined vmachine domain"
    rescue
      # check if the domain is already used
      begin
        Vmachine.find_domain_by_name params[:name]
        return {:success => false, :message => "Failed to create vmachine domain! Domain name '#{params[:name]}' already used!"}
      rescue
        # domain name not used, so this error is left for the "return" below
      end
      return {:success => false, :message => "Failed to create vmachine domain!"}
    end

    resource_list = [params[:hda], params[:hdb], params[:cdrom]].concat params[:depend].split
    resource_list = resource_list.select {|r| r != nil and r != ""}
    resource_list = resource_list.uniq

    Vmachine.log params[:name], "Required resource: #{resource_list.join ','}"

    begin
      args = {
        :name => params[:name],
        :uuid => dom.uuid,
        :hda => params[:hda],
        :hdb => params[:hdb],
        :cdrom => params[:cdrom],
        :resource_list => resource_list
      }
      MiddleMan.worker(:start_vmachine_worker).async_start_vmachine(:arg => args)
      Vmachine.log params[:name], "Preparing to start vmachine"
      return {:success => true, :message => "Successfully created vmachine domain with name='#{dom.name}' and UUID=#{dom.uuid}. It is starting right now."}
    rescue
      Vmachine.log params[:name], "Failed to start vmachine"
      return {:success => false, :message => "Failed to push 'start vmachine' request into job queue! Vmachine UUID=#{dom.uuid}."}
    end
  end

  # non-blocking, most work is delegated to stop_vmachine_worker
  def Vmachine.stop uuid
    # TODO stop a domain, and inform the update_vmachine_worker to upload it, if necessary
    Vmachine.libvirt_action "stop", uuid
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
    # TODO destroy is a complicated process
    Vmachine.libvirt_action "destroy", uuid
    Vmachine.libvirt_action "undefine", uuid
    # cleanup work is left for supervisor_worker, we just destroy the domain
  end

private

  def Vmachine.libvirt_action action_name, uuid
    begin
      dom = Vmachine.find_domain_by_uuid uuid
    rescue
      return {:success => false, :message => "Cannot find vmachine with UUID=#{uuid}!"}
    end

    begin
      dom.send action_name
      return {:success => true, :message => "Successfully processed action '#{action_name}' on vmachine with UUID=#{uuid}."}
    rescue
      return {:success => false, :message => "Failed to process action '#{action_name}' on vmachine with UUID=#{uuid}!"}
    end
  end
  
end

