require "utils"
require "libvirt"
require "fileutils"
require "uuidtools"

# This is the model for virtual machines. We use this class to controll virtual machines.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3
class Vmachine < ActiveRecord::Base

  @@virt_conn = Libvirt::open("qemu:///system")

  def Vmachine.virt_conn
    @@virt_conn
  end

  def Vmachine.all_names
    all_domains.collect {|dom| dom.name}
  end

  def Vmachine.all_domains
    virt_conn = Vmachine.virt_conn

    all_domains = []
    Vmachine.all.each do |vm_model|
      begin
        all_domains << virt_conn.lookup_domain_by_uuid(vm_model.uuid)
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

  # Check if params are correct, when creating new vm.
  #
  # Since::     0.3
  def Vmachine.validate_params params
    ["cpu_count", "mem_size", "name", "hypervisor", "sys_arch", "hda_image", "run_agent", "uuid"].each do |item|
      if params[item] == nil or params[item] == ""
        raise "please provide \"#{item}\"!"
      end
    end

    ["cpu_count", "mem_size"].each do |num_item|
      if params[num_item].to_i.to_s != params[num_item]
        raise "incorrect \"#{cpu_count}\" value!"
      end
    end
  end

  def Vmachine.emit_domain_xml params
    if params[:hda_image] != nil and params[:hda_image] != ""
      FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
      #if VdiskNaming::vdisk_type(params[:hda]).start_with? "sys"
      #  real_hda_filename = "vd-notsaved-#{params[:uuid]}-hda.qcow2"
      #else
        real_hda_filename = params[:hda]
      #end
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

  # Define a new vm domain.
  # Called in vmachines_controller.rb
  #
  # Since::     0.3
  def Vmachine.define params
    Vmachine.validate_params params
    xml_desc = Vmachine.emit_domain_xml params
    dom = Vmachine.virt_conn.define_domain_xml xml_desc
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

      # create the vmachine model, this is a stupid work around
      # for ruby-libvirt's bug: list_defined_domain causes crash
      vm_model = Vmachine.new
      vm_model.uuid = params[:uuid]
      vm_model.name = params[:name]
      vm_model.save
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
    vm_model = Vmachine.find_by_uuid uuid
    Vmachine.delete vm_model if vm_model != nil
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
    # destroy is a complicated process
    begin
      Vmachine.libvirt_action "destroy", uuid
      Vmachine.libvirt_action "undefine", uuid
    ensure
      # this function is ensured to return success, even there might have error in destroy process!
      vm_model = Vmachine.find_by_uuid uuid
      Vmachine.delete vm_model if vm_model != nil
      return {:success => true, :message => "Successfully destroyed vmachine with UUID=#{uuid}."}
    end

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

