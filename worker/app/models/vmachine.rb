require "utils"
require "libvirt"
require "fileutils"
require "yaml"
require "pathname"

# This is the model for virtual machines. We use this class to controll virtual machines.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3
class Vmachine < ActiveRecord::Base

  # libvirt VM status
  LIBVIRT_RUNNING = 1
  LIBVIRT_BLOCK = 2
  LIBVIRT_SUSPENDED = 3
  LIBVIRT_NOT_RUNNING = 5

  HYPERVISOR=common_conf["hypervisor"]
  WORKER_UUID = File.read(File.join(RAILS_ROOT, "config", "worker.uuid"))

  # hypervisor used by nova
  # Connection to libvirt.
  #
  # Since::   0.3
  @@virt_conn = nil
  case HYPERVISOR
  when "xen"
    @@virt_conn = Libvirt::open("xen:///")
  when "kvm"
    @@virt_conn = Libvirt::open("qemu:///system")
  else
    raise "vmachine initial: unsupported hypervisor: #{HYPERVISOR}"
  end

  # Get the connection to libvirt.
  #
  # Since::   0.3
  def Vmachine.virt_conn
    @@virt_conn
  end

  def Vmachine.valid str
    str and str.length > 0
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
    vm_root = Setting.vm_root
    virt_conn = Vmachine.virt_conn
    all_domains = []

    # no longer searching all dirs in the vm_root
    # this will cause heavy burden to NFS server when lots of worker is running
=begin
    Dir.foreach(Setting.vm_root) do |vm_entry|
      vm_dir_path = File.join Setting.vm_root, vm_entry
      next if vm_entry.start_with? "."
      next unless File.directory? vm_dir_path

      begin
        all_domains << virt_conn.lookup_domain_by_name(vm_entry)
      rescue
        # when the VM is being prepared, or being saved, it will not be found by lookup_domain_by_name
        # in this case, we just ignore it
      end
    end
=end
    # steps:
    # active list = virt_conn.list_domains, this returns id list, 0 is the dom-0(host OS)
    # inactive list = virt_conn.list_defined_domains, this returns name-list
    # handle 2 lists above, if vm_dir exists, add it into all_domains
    active_list = virt_conn.list_domains
    inactive_list = virt_conn.list_defined_domains
    active_list.each do |vm_id|
      if vm_id != 0 #ignore dom-0
        begin
          dom = virt_conn.lookup_domain_by_id(vm_id)
          vm_dir_path = File.join vm_root, dom.name
          if File.directory? vm_dir_path #TODO simple statement, checking xml file in the vm_dir_path and compare vm.uuid to dom.uuid is better
            all_domains << dom
          end
        rescue
          #do nothing
        end
      end
    end

    inactive_list.each do |vm_name|
      begin
        dom = virt_conn.lookup_domain_by_name(vm_name)
        vm_dir_path = File.join vm_root, dom.name
        if File.directory? vm_dir_path
          all_domains << dom
        end
      rescue

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
  
  # Helper function that emits corresponding XML for kvm
  #
  # Since::     0.3.3
  def Vmachine.emit_domain_xml_kvm params
    nova_conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    return <<XML_DESC
<domain type='#{params[:hypervisor]}'>
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
  <emulator>/usr/bin/kvm</emulator>
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
  end
  
  # Helper function that emits corresponding XML for xen+img image
  #
  # Since::     0.3.3
  def Vmachine.emit_domain_xml_xen_img params
    return <<XML_DESC
<domain type='#{params[:hypervisor]}'>
<name>#{params[:name]}</name>
<uuid>#{params[:uuid]}</uuid>
<memory>#{params[:mem_size].to_i * 1024}</memory>
<vcpu>#{params[:cpu_count]}</vcpu>
<os>
  #{
    if valid(params[:use_hvm]) and params[:use_hvm].to_s == "true"
      "<type arch='#{params[:arch]}' machine='pc'>hvm</type>\n\
      <loader>/usr/lib/xen/boot/hvmloader</loader>\n"
    else
      "<type arch='#{params[:arch]}' machine='pc'>linux</type>"
    end
  }
  #{if valid(params[:kernel]) and valid(params[:initrd])
 "<kernel>#{params[:kernel]}</kernel>\n\
  <initrd>#{params[:initrd]}</initrd>\n"
    end
   }
  #{if valid(params[:hda_dev])
      "<cmdline>root=/dev/#{params[:hda_dev]} ro </cmdline>"
    end
   }
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
  #{
    if valid(params[:use_hvm]) and params[:use_hvm].to_s == "true"
      "<apic/>\n"
    end
  }
</features>
<devices>
  #{
    if valid(params[:use_hvm]) and params[:use_hvm].to_s == "true"
      if File.exists? "/usr/lib/xen/bin/qemu-dm"
        "<emulator>/usr/lib/xen/bin/qemu-dm</emulator>\n"
      elsif File.exists? "/usr/lib64/xen/bin/qemu-dm"
        "<emulator>/usr/lib64/xen/bin/qemu-dm</emulator>\n"
      else
        raise "qemu-dm not found!"
      end
    end
  }
  <disk type='file' device='disk'>
    <driver name='file'/>
    <source file='#{Setting.vm_root}/#{params[:name]}/#{params[:hda_image]}'/>
    #{
      if valid(params[:hda_dev])
        "<target dev='#{params[:hda_dev]}' bus='scsi'/>"
      else
        "<target dev='xvda' bus='xen'/>"
      end
     }
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
    <source bridge='xenbr0'/>
    <mac address='54:7E:#{
# generate random mac address
# note that mac address has some format requirements
((1..4).collect {|n| "%02x" % (256 * rand)}).join ":"
}'/>
    <script path='/etc/xen/scripts/vif-bridge'/>
    <target dev='vif-1.0'/>
  </interface>
  <serial type='pty'>
    <source path='/dev/pts/1'/>
    <target port='0'/>
  </serial>
  <console type='pty' tty='/dev/pts/1'>
    <source path='/dev/pts/1'/>
    <target port='0'/>
  </console>
  <input type='tablet' bus='usb'/>
  <input type='mouse' bus='ps2'/>
  <graphics type='vnc' port='-1' listen='0.0.0.0'/>
</devices>
</domain>
XML_DESC
  end

  # Helper function that emits corresponding XML for xen+qcow image
  #
  # Since::     0.3.3
  def Vmachine.emit_domain_xml_xen_qcow params
    nova_conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    return <<XML_DESC
<domain type='xen'>
<name>#{params[:name]}</name>
<uuid>#{params[:uuid]}</uuid>
<memory>#{params[:mem_size].to_i * 1024}</memory>
<vcpu>#{params[:cpu_count]}</vcpu>
<os>
 #{
   if valid(params[:use_hvm]) and params[:use_hvm].to_s == "true"
     "<type>hvm</type>\n\
     <loader>/usr/lib/xen/boot/hvmloader</loader>\n
     <boot dev='hd'/>"
   else
     "<type arch='#{params[:arch]}' machine='pc'>linux</type>"
   end
 }
 #{if valid(params[:kernel]) and valid(params[:initrd])
"<kernel>#{params[:kernel]}</kernel>\n\
 <initrd>#{params[:initrd]}</initrd>\n"
   end
  }
 #{if valid(params[:hda_dev])
     "<cmdline>root=/dev/#{params[:hda_dev]} ro </cmdline>"
   end
  }
</os>
<features>
  <acpi/>
  #{
    if valid(params[:use_hvm]) and params[:use_hvm].to_s == "true"
      "<apic/>\n"
    end
  }
  <pae/>
</features>
<clock offset='utc'/>
<on_poweroff>destroy</on_poweroff>
<on_reboot>restart</on_reboot>
<on_crash>restart</on_crash>
<devices>
#{
  if valid(params[:use_hvm]) and params[:use_hvm].to_s == "true"
    if File.exists? "/usr/lib/xen/bin/qemu-dm"
      "<emulator>/usr/lib/xen/bin/qemu-dm</emulator>\n"
    elsif File.exists? "/usr/lib64/xen/bin/qemu-dm"
      "<emulator>/usr/lib64/xen/bin/qemu-dm</emulator>\n"
    else
      raise "qemu-dm not found!"
    end
  end
}
  <disk type='file' device='disk'>
    <driver name='tap' type='qcow'/>
    <source file='#{Setting.vm_root}/#{params[:name]}/#{params[:hda_image]}'/>
    <target dev='hda' bus='ide'/>
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
}
  <interface type='bridge'>
    <mac address='54:7E:#{
  # generate random mac address
  # note that mac address has some format requirements
  ((1..4).collect {|n| "%02x" % (256 * rand)}).join ":"
  }'/>
    <source bridge='xenbr0'/>
    <script path='/etc/xen/scripts/vif-bridge'/>
    <target dev='vif4.0'/>
  </interface>
  <serial type='pty'>
    <source path='/dev/pts/1'/>
    <target port='0'/>
  </serial>
  <console type='pty' tty='/dev/pts/1'>
    <source path='/dev/pts/1'/>
    <target port='0'/>
  </console>
  <input type='tablet' bus='usb'/>
  <input type='mouse' bus='ps2'/>
  <graphics type='vnc' port='-1' autoport='yes' listen='0.0.0.0'/>
</devices>
</domain>
XML_DESC
  end

  # Generate XML definition on VM params. It will be used by libvirt.
  # * Note on generated XML:
  #   VNC port set to -1, so libvirt will automatically select a VNC port.
  #   Input device set to usb tablet, this will make mouse pointer work better in VNC.
  #
  # Since::     0.3
  def Vmachine.emit_domain_xml params
    xml_desc = ""
    case params[:hypervisor]
    when "kvm"
      xml_desc = Vmachine.emit_domain_xml_kvm params
    when "xen"
      if params[:hda_image] =~ /img$/
        xml_desc = Vmachine.emit_domain_xml_xen_img params
      elsif params[:hda_image] =~ /qcow$/
        xml_desc = Vmachine.emit_domain_xml_xen_qcow params        
      else
        raise "disk image #{params[:hda_image]} not supported!"
      end
    else
      raise "hypervisor #{params[:hypervisor]} not supported!"
    end
    File.open("/var/xml_desc.log", "w") do |f|
      f.puts xml_desc
    end
    puts xml_desc
    File.open("/var/xml_desc.log", "w") do |f|
        f.puts xml_desc
    end
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
      # start background helper
      Vmachine.prepare_vm_dir params
      #Vmachine.start_vm_daemon params[:name]
    rescue => e
      return {:success => false, :message => e.to_s}
    end

    return {:success => true, :message => "vm named '#{params[:name]}' defined, it will be started soon."}
  end

  # Suspend a vmachine. This is a blocking call, but won't take a long time.
  # You must provide either uuid or name of the VM.
  #
  # Since::     0.3

  def Vmachine.restart params
    vm_name = params[:name]
    if vm_name and vm_name != ""
      Vmachine.send_instruction vm_name, "restart"
    else
      raise "Please provide a name!"
    end
  end

  def Vmachine.suspend params
    vm_name = params[:name]

    if vm_name and vm_name != ""
      Vmachine.send_instruction vm_name, "suspend"
    else
      raise "Please provide a name!"
    end
=begin
    if params[:uuid] != nil and params[:uuid] != ""
      Vmachine.libvirt_call_by_uuid "suspend", params[:uuid]
    elsif params[:name] != nil and params[:name] != ""
      Vmachine.libvirt_call_by_name "suspend", params[:name]
    else
      raise "Please provide either uuid or name!"
    end
=end
  end

  # Resume a vmachine. This is a blocking call, but won't take a long time.
  # You must provide either uuid or name of the VM.
  #
  # Since::     0.3

  # Now it's a non-blocking call, which only sends instruction and returns
  def Vmachine.resume params
    vm_name = params[:name]

    if vm_name and vm_name != ""
      Vmachine.send_instruction vm_name, "resume"
    else
      raise "Please provide a name!"
    end
=begin
    if params[:uuid] != nil and params[:uuid] != ""
      Vmachine.libvirt_call_by_uuid "resume", params[:uuid]
    elsif params[:name] != nil and params[:name] != ""
      Vmachine.libvirt_call_by_name "resume", params[:name]
    else
      raise "Please provide either uuid or name!"
    end
=end
  end

  # Destroy a VM domain, either by name or by uuid. Its hda image will not be saved.
  # The clean up work is left for background workers.
  #
  # * This is a blocking method!
  #
  # * When both name & uuid is given, we work according to "uuid" value.
  #
  # Since::     0.3

  # non-blocking Since 0.31

  def Vmachine.destroy params
    vm_name = params[:name]
    if vm_name and vm_name != ""
      Vmachine.kill_vm_daemon vm_name
      sleep 1
      Vmachine.send_instruction vm_name, "destroy"
    else
      raise "Please provide a name!"
    end

=begin
    if params[:uuid] != nil and params[:uuid].is_uuid?
      begin
        # "destroy" must be performed on running vm, so when vm is not running,
        # this will trigger an exception. we have to catch it by "rescue"
        Vmachine.libvirt_call_by_uuid "destroy", params[:uuid]
      rescue
      end
      begin
        Vmachine.libvirt_call_by_uuid "undefine", params[:uuid]
      rescue
      end
      return {:success => true, :message => "destroyed vm with uuid #{params[:uuid]}."}

    elsif params[:name] != nil and params[:name] != ""
      begin
        # "destroy" must be performed on running vm, so when vm is not running,
        # this will trigger an exception. we have to catch it by "rescue"
        Vmachine.libvirt_call_by_name "destroy", params[:name]
      rescue
      end
      begin
        Vmachine.libvirt_call_by_name "undefine", params[:name]
      rescue
      end
      return {:success => true, :message => "destroyed vm named '#{params[:name]}'."}

    else
      raise "you must provide either 'name' or 'uuid'!"
    end
=end
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

  # send instruction to vm_daemon by writing file 'action' in the vm_dir
  # since all operation will be executed in async mode
  def Vmachine.send_instruction vm_name, instruction
    begin
      Vmachine.open_vm_file(vm_name, "action") do |f|
        f.write instruction
      end
      Vmachine.log vm_name, "Instruction '#{instruction}' has been sent to #{vm_name}"
      begin
        Vmachine.check_vm_daemon vm_name
        return {:success => true, :message => "Instruction '#{instruction}' has been sent to #{vm_name}"}
      rescue
        Vmachine.log vm_name "Failed while checking vm_daemon for #{vm_name}!"
        return {:success => false, :message => "failed while checking vm_daemon for #{vm_name}!"}
      end
    rescue
      Vmachine.log vm_name, "Cannot send instruction '#{instruction}' to vm #{vm_name}!"
      raise "Cannot send instruction '#{instruction}' to vm #{vm_name}!"
    end
  end

  # Start the background helper daemon, which prepares resource, and starts the vm.
  #
  # Since::   0.3

  def Vmachine.prepare_vm_dir params
    # the 'required_images' file contains all the required vdisk/iso image for the vm to boot

    # first write worker's uuid into vm_dir, this is helpfup to trash cleaner

    begin
      worker_uuid = File.read(RAILS_ROOT + '/config/worker.uuid')
      Vmachine.open_vm_file(params[:name], "host.uuid") do |f|
        f.write worker_uuid
      end
    rescue
      raise "Couldn't get worker.uuid!"
    end

    Vmachine.open_vm_file(params[:name], "required_images") do |f|
      f.write "#{params[:hda_image]}\n"
      if params[:cd_image] != nil and params[:cd_image] != ""
        f.write "#{params[:cd_image]}\n"
      end
    end

    # the 'agent_packages' file contains all the required packages for agent cd image
    # also, writes the 'nodelist' file
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
      Vmachine.open_vm_file(params[:name], "nodelist") do |f|
        params[:agent_hint].each_line do |line|
          if line.start_with? "nodelist="
            nodelist = ((line[9..-1]).split ',').select {|item| item.length > 0}
            nodelist.each do |node|
              node = node.strip
              f.write "#{node}\n"
            end
          end
        end
      end
    end

    if params[:agent_hint] != nil and params[:agent_hint] != ""
      Vmachine.open_vm_file(params[:name], "agent_hint") do |f|
        f.write params[:agent_hint]
        f.write "\n"
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
      f.write "unprepared"
    end
    Vmachine.log params[:name], "changed vmachine status to 'unprepared'"

    # write instruction file, so vm_daemon_helper will do preparing
    Vmachine.send_instruction params[:name], "prepare"

  end

  def Vmachine.start_vm_daemon vm_name
    # start the vm_daemon for the virtual machine
    pid = fork do
      Dir.chdir "#{RAILS_ROOT}/lib"
      # close all opened df: this prevents occupying server socket
      Dir.foreach("/proc/#{Process.pid}/fd") do |entry|
        next if entry.start_with? "."
        fd = entry.to_i
        if fd > 2
          begin
            #Vmachine.log vm_name, "closing fd #{fd}"
            IO::new(fd).close
          rescue
          end
        end
      end
      exec "./vm_daemon.rb #{RAILS_ROOT} #{File.read "#{RAILS_ROOT}/config/storage_server.conf"} #{File.join Setting.vm_root, vm_name} #{vm_name} #{HYPERVISOR}"
    end
    Process.detach pid  # prevent zombie process
    Vmachine.log vm_name, "vm_daemon started for '#{vm_name}'"
  end

  # check whether vm_daemon exists
  # if not, restart it

  def Vmachine.kill_vm_daemon vm_name
    #check host.uuid == worker.uuid?
    #if not, cannot kill

    vm_dir = File.join Setting.vm_root, vm_name
    host_uuid_fn = File.join vm_dir, "host.uuid"

    vm_daemon_pid_fn = File.join vm_dir, "vm_daemon.pid"
    vm_daemon_pid = nil
    host_uuid = nil
    begin
      host_uuid = File.read host_uuid_fn
    rescue
    end

    if host_uuid and host_uuid == WORKER_UUID

      begin
        vm_daemon_pid = File.read vm_daemon_pid_fn
      rescue
      end
      if vm_daemon_pid
        my_exec "kill -9 #{vm_daemon_pid}"
      end
    else
      #host_uuid = nil
      #this should be nutralized by cleaner!
    end
  end


  def Vmachine.check_vm_daemon vm_name
    vm_dir = File.join Setting.vm_root, vm_name
    vm_daemon_pid_fn = File.join vm_dir, "vm_daemon.pid"
    vm_daemon_pid = nil
    begin
      vm_daemon_pid = File.read vm_daemon_pid_fn
    rescue
      vm_daemon_pid = nil
    end

    if vm_daemon_pid
      begin
        Process.kill 0, vm_daemon_pid.to_i
        Vmachine.log vm_name, "[debug] vm_daemon exists, pid is #{vm_daemon_pid}"
      rescue
        # start new vm daemon
        Vmachine.log vm_name, "[debug] vm_daemon doesn't exist, restart it"
        FileUtils.rm_f vm_daemon_pid_fn rescue nil
        start_vm_daemon vm_name
      end
    else
        Vmachine.log vm_name, "[debug] vm_daemon doesn't exist, start it"
        start_vm_daemon vm_name
    end
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

  def Vmachine.live_migrate_to vm_name, migrate_dest, migrate_src
    migrate_to_fn = File.join Setting.vm_root, vm_name, "migrate_to"
    File.open(migrate_to_fn, "w") do |f|
      f.write migrate_dest
    end
    migrate_from_fn = File.join Setting.vm_root, vm_name, "migrate_from"
    if migrate_src
      File.open(migrate_from_fn, "a") do |f|
        f.write migrate_src
      end
    end
    Vmachine.send_instruction vm_name, "migrate"

    Vmachine.log vm_name, "prepare migrating to #{migrate_dest}"
    return {:success => true, :message => "Vmachine '#{vm_name}' is preparing migrate to worker '#{migrate_dest}'"}
  end


  # Since we use an async model, migration should be executed by vm_daemon_helper

  def Vmachine.xen_live_migrate params
    #TODO:we can have a type in params to divide xen and kvm
    raise "Should not be here"
    if params[:dst] != nil and params[:dst] != ""
      if params[:uuid] != nil and params[:uuid] != ""
        begin
          system "virsh migrate --live " + params[:uuid].to_s + \
            " xen:/// xenmigr://" + params[:dst].to_s
        rescue
          raise "xen live migrate failed! Can't migrate #{params[:uuid]} to #{params[:dst]}"
        end
        return {:success => true}
      elsif params[:name] != nil and params[:name] != ""
        begin
          system "virsh migrate --live " + params[:name].to_s + \
            " xen:/// xenmigr://" + params[:dst].to_s
        rescue
          raise "xen live migrate failed! Can't migrate #{params[:name]} to #{params[:dst]}"
        end
      else
        raise "Please provide either uuid or name!"
      end
    else
      raise "Please provide a destination machine ip or url!"
    end
  end


end

