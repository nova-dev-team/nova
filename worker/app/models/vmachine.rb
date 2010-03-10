require "utils"
require "libvirt"
require "fileutils"

# This is the model for virtual machines. We use this class to controll virtual machines.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3
class Vmachine < ActiveRecord::Base

  # libvirt VM status
  LIBVIRT_RUNNING = 1
  LIBVIRT_SUSPENDED = 3
  LIBVIRT_NOT_RUNNING = 5

  # Connection to libvirt.
  #
  # Since::   0.3
  @@virt_conn = Libvirt::open("qemu:///system")

  # Get the connection to libvirt.
  #
  # Since::   0.3
  def Vmachine.virt_conn
    @@virt_conn
  end

  # List all vmachines by their name.
  #
  # Since::   0.3
  def Vmachine.all_names
    all_domains.collect {|dom| dom.name}
  end

  # List all vmachine domains.
  #
  # Since::   0.3
  def Vmachine.all_domains
    virt_conn = Vmachine.virt_conn
    all_domains = []
    Vmachine.all.each do |vm_model|
      begin
        all_domains << virt_conn.lookup_domain_by_uuid(vm_model.uuid)
      rescue
        # vmachine not found by libvirt, delete it!
        Vmachine.delete vm_model
        next
      end
    end
    return all_domains
  end

  # Find a vm domain by uuid
  #
  # Since::   0.3
  def Vmachine.find_domain_by_uuid uuid
    Vmachine.virt_conn.lookup_domain_by_uuid uuid
  end

  # Find a vm domain by name
  #
  # Since::   0.3
  def Vmachine.find_domain_by_name name
    Vmachine.virt_conn.lookup_domain_by_name name
  end

  # Check if params are correct, before creating new vm.
  #
  # * Required params:
  #   cpu_count:  number of CPUs
  #   mem_size:   memory size
  #   name:       name of the VM
  #   hypervisor: which hypervisor do we use?
  #   arch:       hardware architecture
  #   hda_image:  uri of hard disk 1 (hda) image
  #   run_agent:  which agent do we need to run
  #   uuid:       uuid of the VM
  #
  # Since::     0.3
  def Vmachine.validate_params params
    ["cpu_count", "mem_size", "name", "hypervisor", "arch", "hda_image", "run_agent", "uuid"].each do |item|
      if params[item] == nil or params[item] == ""
        raise "please provide \"#{item}\"!"
      end
    end

    ["cpu_count", "mem_size"].each do |num_item|
      if params[num_item].to_i.to_s != params[num_item]
        raise "incorrect \"#{cpu_count}\" value!"
      end
    end
    
    unless params["uuid"].is_uuid?
      raise "malformed uuid!"
    end

    unless ["x86", "x86_64", "amd64", "i686"].include? params["arch"]
      raise "unsupported architecture"
    end
    
  end

  # Generate XML definition on VM params. It will be used by libvirt.
  # * Note on generated XML:
  #   VNC port set to -1, so libvirt will automatically select a VNC port.
  #   Input device set to usb tablet, this will make mouse pointer work better in VNC.
  #
  # Since::     0.3
  def Vmachine.emit_domain_xml params
    xml_desc = <<XML_DESC
<domain type='qemu'>
  <name>#{params[:name]}</name>
  <uuid>#{params[:uuid]}</uuid>
  <memory>#{params[:mem_size].to_i * 1024}</memory>
  <vcpu>#{params[:cpu_count]}</vcpu>
  <os>
    <type arch='#{params[:arch]}' machine='pc'>hvm</type>
    <boot dev='hd'/>
  </os>
  <features>
    <pae/>
    <acpi/>
  </features>
  <devices>
    <emulator>#{
# determine emulator from "hypervisor" param
case params[:hypervisor]
when "kvm"
  "/usr/bin/kvm"
else
  raise "hypervisor '#{params[:hypervisor]}' not supported!"
end
}</emulator>
    <disk type='file' device='disk'>
      <source file='#{
# TODO determine hda image name
# TODO make agent cd
params[:hda_image]
}'/>
      <target dev='hda'/>
    </disk>
    <disk type='file' device='cdrom'>
      <source file='agent-cd.iso'/>
      <target dev='cdrom'/>
    </disk>
    <interface type='bridge'>
      <source bridge='#{
# TODO nova br, read from common/config
"br0"
}'/>
      <mac address='#{
# TODO generate mac addr  
"TODO"
}'/>
    </interface>
    <graphics type='vnc' port='-1' listen='0.0.0.0'/>
    <input type='tablet' bus='usb'/>
  </devices>
</domain>
XML_DESC
    puts xml_desc
    return xml_desc
  end

  # Define a new vm domain.
  # Called in vmachines_controller.rb
  #
  # Since::     0.3
  def Vmachine.define params
    Vmachine.validate_params params
    xml_desc = Vmachine.emit_domain_xml params

    # define the domain!
    dom = Vmachine.virt_conn.define_domain_xml xml_desc

    # write config files in VM working dir
    Vmachine.open_vm_file(params[:name], "xml_desc.xml") do |f|
      f.write xml_desc
    end
    Vmachine.open_vm_file(params[:name], "status") do |f|
      f.write "defined"
    end
    Vmachine.log params[:name], "virtual machine defined"
  end

  # Create a new domain, and start it.
  # This call is non-blocking, it just uses libvirt to define VM domain, and write a few config
  # files. A background worker will detect the newly created domain, prepares all required resource,
  # and then starts the VM.
  #
  # Since::     0.3
  def Vmachine.start params
    begin
      dom = Vmachine.define params

      # start background helper
      Vmachine.start_vm_daemon params
      return {:success => true, :message => "vm named '#{params[:name]}' defined, it will be started soon."}
    rescue => e
      # check if domain name already used
      begin
        Vmachine.find_domain_by_name params[:name]
        return {:success => false, :message => "vm name '#{params[:name]}' already used!"}
      rescue
        # domain name not used
      end

      # check if domain uuid already used
      begin
        dom = Vmachine.find_domain_by_uuid params[:uuid]
        return {:success => false, :message => "uuid #{params[:uuid]} already used by vm named '#{dom.name}'!"}
      rescue
        # domain uuid not used
      end

      return {:success => false, :message => e.to_s}
    end
  end

  # non-blocking, most work is delegated to stop_vmachine_worker
  # TODO make it like a "power-off", hda-image will be saved
  def Vmachine.stop uuid
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
  # Destroy a VM domain, either by name or by uuid. Its hda image will not be saved.
  # The clean up work is left for background workers.
  #
  # * When both name & uuid is given, we work according to "uuid" value.
  #
  # Since::     0.3
  def Vmachine.destroy params
    if params[:uuid] != nil and params[:uuid].is_uuid?
      begin
        # "destroy" must be performed on running vm, so when vm is not running,
        # this will trigger an exception. we have to catch it by "rescue"
        Vmachine.libvirt_call_by_uuid "destroy", params[:uuid]
      rescue
      end
      Vmachine.libvirt_call_by_uuid "undefine", params[:uuid]
      vm_model = Vmachine.find_by_uuid params[:uuid]
      Vmachine.delete vm_model if vm_model != nil
      return {:success => true, :message => "destroyed vm with uuid #{params[:uuid]}."}

    elsif params[:name] != nil and params[:name] != ""
      begin
        # "destroy" must be performed on running vm, so when vm is not running,
        # this will trigger an exception. we have to catch it by "rescue"
        Vmachine.libvirt_call_by_name "destroy", params[:name]
      rescue
      end
      Vmachine.libvirt_call_by_name "undefine", params[:name]
      vm_model = Vmachine.find_by_name params[:name]
      Vmachine.delete vm_model if vm_model != nil
      return {:success => true, :message => "destroyed vm named '#{params[:name]}'."}

    else
      raise "you must provide either 'name' or 'uuid'!"
    end
  end

private

  # Make a call to libvirt, with given action name, and vm uuid.
  #
  # Since::     0.3
  def Vmachine.libvirt_call_by_uuid action_name, uuid
    begin
      dom = Vmachine.find_domain_by_uuid uuid
    rescue
      raise "cannot find vm with uuid #{uuid}!"
    end

    begin
      dom.send action_name
      return {:success => true, :message => "action '#{action_name}' on vm with uuid #{uuid} is done."}
    rescue
      raise "action '#{action_name}' on vm with uuid #{uuid} has failed!"
    end
  end

  # Make a call to libvirt, with given action name, and vm name.
  #
  # Since::     0.3
  def Vmachine.libvirt_call_by_name action_name, name
    begin
      dom = Vmachine.find_domain_by_name name
    rescue
      raise "cannot find vm with name '#{name}'!"
    end

    begin
      dom.send action_name
      return {:success => true, :message => "action '#{action_name}' on vm with name '#{name}' is done."}
    rescue
      raise "action '#{action_name}' on vm with name '#{name}' has failed!"
    end
  end

  # A helper function that opens a file (typically config/log file) for a vm.
  # The vm's working directory will be created if not exist.
  #
  # Since::   0.3
  def Vmachine.open_vm_file vm_name, file_name, open_mode = "w"
    vm_dir = File.join Setting.vm_root, vm_name
    FileUtils.mkdir_p vm_dir unless File.exists? vm_dir
    File.open((File.join vm_dir, file_name), open_mode) do |f|
      yield f
    end
  end

  # Start the background helper daemon, which prepares resource, and starts the vm.
  #
  # Since::   0.3
  def Vmachine.start_vm_daemon params
    # TODO write lftp scripts for downloading resource
    Vmachine.open_vm_file(params[:name], "lftp.retr.script") do |f|
    end
    
    # TODO write lftp scripts for uploading resource
    Vmachine.open_vm_file(params[:name], "lftp.stor.script") do |f|
    end

    # change status to preparing
    Vmachine.open_vm_file(params[:name], "status") do |f|
      f.write "preparing"
    end

    Vmachine.log params[:name], "preparing resources for vmachine '#{params[:name]}'"

    # start the vm_daemon for the virtual machine
    pid = fork do
      chdir "#{RAILS_ROOT}/lib"
      exec "./vm_daemon #{File.join Setting.vm_root, params[:name]}"
    end
    Process.detach pid  # prevent zombie process

    Vmachine.log params[:name], "vm_daemon started for '#{params[:name]}'"
  end

  # Write logs for the virtual machine.
  #
  # Since::     0.3
  def Vmachine.log vm_name, message
    Vmachine.open_vm_file(vm_name, "log", "a") do |f|
      message.each_line do |line|
        if line.end_with? "\n"
          f.write "[#{Time.now}] #{line}"
        else
          f.write "[#{Time.now}] #{line}\n"
        end
      end
    end
  end

end

