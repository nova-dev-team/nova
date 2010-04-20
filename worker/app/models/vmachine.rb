require "utils"
require "libvirt"
require "fileutils"
require "yaml"

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
    
    Dir.foreach(Setting.vm_root) do |vm_entry|
      vm_dir_path = File.join Setting.vm_root, vm_entry
      next if vm_entry.start_with? "."
      next unless File.directory? vm_dir_path

      begin
        all_domains << virt_conn.lookup_domain_by_name(vm_entry)
      rescue
        # failed to find the vm, mark it as "destroyed"
        Vmachine.log vm_entry, "Failed to lookup VM domain with name='#{vm_entry}'"
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
    nova_conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    xml_desc = <<XML_DESC
<domain type='kvm'>
  <name>#{params[:name]}</name>
  <uuid>#{params[:uuid]}</uuid>
  <memory>#{params[:mem_size].to_i * 1024}</memory>
  <vcpu>#{params[:cpu_count]}</vcpu>
  <os>
    <type arch='#{params[:arch]}' machine='pc'>hvm</type>
    <boot dev='#{
# if used user's custom cd image, we boot from cdrom
if params[:cd_image] != nil and params[:cd_image] != ""
  "cdrom"
else
  "hd"
end
}'/>
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
      <source file='#{Setting.vm_root}/#{params[:name]}/#{params[:hda_image]}'/>
      <target dev='hda'/>
    </disk>
#{
# determine cdrom
if params[:run_agent].to_s == "true"
"    <disk type='file' device='cdrom'>
      <source file='#{Setting.vm_root}/#{params[:name]}/agent-cd.iso'/>
      <target dev='hdc'/>
    </disk>
"
elsif params[:cd_image] != nil and params[:cd_image] != ""
"    <disk type='file' device='cdrom'>
      <source file='#{Setting.vm_root}/#{params[:name]}/#{params[:cd_image]}'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
"
end
}    <interface type='bridge'>
      <source bridge='#{nova_conf["vm_network_bridge"]}'/>
      <mac address='54:7E:#{
# generate random mac address
# note that mac address has some format requirements
((1..4).collect {|n| "%02x" % (256 * rand)}).join ":"
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
    Vmachine.open_vm_file(params[:name], "params") do |f|
      params.each do |key, value|
        f.write "#{key}=#{value}\n"
      end
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

    begin
      # start background helpe
      Vmachine.start_vm_daemon params
    rescue => e
      return {:success => false, :message => e.to_s}
    end

    return {:success => true, :message => "vm named '#{params[:name]}' defined, it will be started soon."}
  end

  # Suspend a vmachine. This is a blocking call, but won't take a long time.
  # You must provide either uuid or name of the VM.
  #
  # Since::     0.3
  def Vmachine.suspend params
    if params[:uuid] != nil and params[:uuid] != ""
      Vmachine.libvirt_call_by_uuid "suspend", params[:uuid]
    elsif params[:name] != nil and params[:name] != ""
      Vmachine.libvirt_call_by_name "suspend", params[:name]
    else
      raise "Please provide either uuid or name!"
    end
  end

  # Resume a vmachine. This is a blocking call, but won't take a long time.
  # You must provide either uuid or name of the VM.
  #
  # Since::     0.3
  def Vmachine.resume params
    if params[:uuid] != nil and params[:uuid] != ""
      Vmachine.libvirt_call_by_uuid "resume", params[:uuid]
    elsif params[:name] != nil and params[:name] != ""
      Vmachine.libvirt_call_by_name "resume", params[:name]
    else
      raise "Please provide either uuid or name!"
    end
  end

  # Destroy a VM domain, either by name or by uuid. Its hda image will not be saved.
  # The clean up work is left for background workers.
  #
  # * This is a blocking method!
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
      return {:success => true, :message => "destroyed vm with uuid #{params[:uuid]}."}

    elsif params[:name] != nil and params[:name] != ""
      begin
        # "destroy" must be performed on running vm, so when vm is not running,
        # this will trigger an exception. we have to catch it by "rescue"
        Vmachine.libvirt_call_by_name "destroy", params[:name]
      rescue
      end
      Vmachine.libvirt_call_by_name "undefine", params[:name]
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

    # the 'required_images' file contains all the required vdisk/iso image for the vm to boot
    Vmachine.open_vm_file(params[:name], "required_images") do |f|
      f.write "#{params[:hda_image]}\n"
      if params[:cd_image] != nil and params[:cd_image] != ""
        f.write "#{params[:cd_image]}\n"
      end
    end

    # the 'agent_packages' file contains all the required packages for agent cd image
    if params[:run_agent].to_s == "true" and params[:agent_hint] != nil and params[:agent_hint] != ""
      Vmachine.open_vm_file(params[:name], "agent_packages") do |f|
        params[:agent_hint].each_line do |line|
          if line.start_with? "agent_packages="
            pkgs = (line[15..-1].split /,| |\n/).select {|item| item.length > 0}
            pkgs.each do |pkg|
              f.write "#{pkg}\n"
            end
          end
        end
      end
    end

    if params[:agent_hint] != nil and params[:agent_hint] != ""
      Vmachine.open_vm_file(params[:name], "agent_hint") do |f|
        f.write params[:agent_hint]
      end
    end
    
    # write the 'hda_save_to' file, which indicates the hda image will be saved
    if params[:hda_save_to] != nil and params[:hda_save_to] != ""
      Vmachine.open_vm_file(params[:name], "hda_save_to") do |f|
        f.write params[:hda_save_to]
      end
    end

    # change status to preparing
    Vmachine.open_vm_file(params[:name], "status") do |f|
      f.write "preparing"
    end

    Vmachine.log params[:name], "changed vmachine status to 'preparing'"

    # start the vm_daemon for the virtual machine
    pid = fork do
      Dir.chdir "#{RAILS_ROOT}/lib"
      # close all opened df: this prevents occupying server socket
      Dir.foreach("/proc/#{Process.pid}/fd") do |entry|
        next if entry.start_with? "."
        fd = entry.to_i
        if fd > 2
          begin
            IO::new(fd).close
          rescue
          end
        end
      end
      exec "./vm_daemon #{File.read "#{RAILS_ROOT}/config/storage_server.conf"} #{File.join Setting.vm_root, params[:name]}"
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

  # Change the hda_save_to value.
  # * If 'hda_save_to' is empty "", the hda_save_to file will be removed.
  #
  # Since::     0.3
  def Vmachine.change_hda_save_to vm_name, hda_save_to
    hda_save_to_fn = File.join Setting.vm_root, vm_name, "hda_save_to"
    if hda_save_to == ""
      if File.exists? hda_save_to_fn
        FileUtils.rm_f hda_save_to_fn
      end
      return {:success => true, :message => "Disabled saving for '#{vm_name}'"}
    else
      File.open(hda_save_to_fn, "w") do |f|
        f.write hda_save_to
      end
      return {:success => true, :message => "Enabled saving for '#{vm_name}', target address is '#{hda_save_to}'"}
    end
  end

end

