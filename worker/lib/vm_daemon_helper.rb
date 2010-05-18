#!/usr/bin/ruby

require 'libvirt'
require 'fileutils'
require 'xmlsimple'
require 'ceil_iso_generator'
require 'uri'

# libvirt status constants
LIBVIRT_RUNNING = 1
LIBVIRT_SUSPENDED = 3
LIBVIRT_NOT_RUNNING = 5

def my_exec cmd
  puts "[cmd] #{cmd}"
  system cmd
end

def write_log message
  File.open("log", "a") do |f|
    message.each_line do |line|
      if line.end_with? "\n"
        f.write "[#{Time.now}] #{line}"
      else
        f.write "[#{Time.now}] #{line}\n"
      end
    end
  end
end

def find_available_copy image_pool_dir, hda_name
  copy_list = []
  Dir.foreach(image_pool_dir) do |entry|
    entry_path = File.join image_pool_dir, entry
    if (entry.start_with? hda_name) == false or entry !~ /\.pool\.[0-9]+$/
      next # skip if it is not a copy
    end
    entry_copying_lock = "#{entry_path}.copying"
    if File.exists? entry_copying_lock
      next # skip if entry is being copied
    end
    copy_list << entry_path
  end
  # choose a copy with largest copy number
  copy_list.sort! do |a, b|
    idx = (a.rindex ".") + 1
    va = a[idx..-1].to_i
    idx = (b.rindex ".") + 1
    vb = b[idx..-1].to_i
    vb <=> va
  end
  return copy_list[0]
end

def prepare_hda_image storage_server, image_pool_dir, vm_dir, hda_name
  # be care of the .copying lock!
  #
  # if hda image exists in image_pool
  #   if there's extra copy, then directly move it to vm_dir, and use it
  #   else there's no extra copy, directly copy it to vm_dir, and use it
  # else there is no hda image in the image_pool
  #   if the base image is already being copied (.copying lock), wait until it is ready, and directly copy it to vm_dir
  #   else the image is not being copied, download it into image_pool, and directly copy it to vm_dir

  base_image_name = File.join image_pool_dir, hda_name
  base_image_copying_lock = File.join image_pool_dir, "#{hda_name}.copying"
  if File.exists? base_image_name and (File.exists? base_image_copying_lock) == false
    # base image exists
    copy = find_available_copy image_pool_dir, hda_name
    if copy != nil
      # there is extra copy! directly move it to vm_dir
      write_log "found available hda image copy '#{copy}', using it directly"
      FileUtils.mv copy, (File.join vm_dir, hda_name)
    else
      # there is no extra copy, directly copy it to vm_dir
      write_log "available hda image copy not found, copying directly"
      my_exec "cp #{base_image_name} #{File.join vm_dir, hda_name}"
    end
  else
    # base image does not exist, we have to download it
    write_log "hda image is not ready, need to be downloaded"
    if File.exists? base_image_name and File.exists? base_image_copying_lock
      # already being downloaded by other process, wait for it
      write_log "download already started by other process, waiting for it"
      while File.exists? base_image_copying_lock
        sleep 1
      end
      write_log "detected hda image download finished"
    else
      # base_image does not exist, create downloading lock, and download it
      write_log "start downloading '#{hda_name}'"

      # create downloading lock
      File.open(base_image_copying_lock, "w") {|f|}
      write_log "created copying lock"
      
      lftp_script_file = File.join image_pool_dir, "#{hda_name}.lftp"
      File.open(lftp_script_file, "w") do |f|
        f.write <<LFTP_SCRIPT_FILE
set net:timeout 10
set net:max-retries 2
set net:reconnect-interval-base 1
open #{storage_server}
cd vdisks
get #{hda_name}
LFTP_SCRIPT_FILE
      end
      write_log "generated lftp download script"
      write_log "calling lftp to do download work"
      my_exec "cd #{image_pool_dir} && lftp -f #{hda_name}.lftp 2>&1 >> #{hda_name}.lftp.log"

      write_log "lftp process finished"

      FileUtils.rm_f lftp_script_file
      write_log "removed generated lftp download script"

      # TODO use lftp log to detect whether download succeeded

      FileUtils.rm_f base_image_copying_lock
      write_log "removed copying lock"

      write_log "finished downloading hda image"
    end
    write_log "retry preparing process"
    prepare_hda_image storage_server, image_pool_dir, vm_dir, hda_name
  end
end

def prepare_iso_image storage_server, image_pool_dir, vm_dir, iso_name
  # be care of the .copying lock!
  #
  # if package exists in the package_pool
  #   make soft link, directly use it
  # else it is not in package_pool
  #   if the package is being copied, wait until it is ready, and make soft link (by calling the function again)
  #   else the package is not copied, download it directly, and make soft link (by calling the function again)

  base_image_path = File.join image_pool_dir, iso_name
  base_image_copying_lock = File.join image_pool_dir, "#{iso_name}.copying"

  dest_image_path = File.join vm_dir, iso_name

  if File.exists? base_image_path and (File.exists? base_image_copying_lock) == false
    # image already available
    write_log "iso image '#{iso_name}' already available, making hard link"
    unless File.exists? dest_image_path
      FileUtils.ln base_image_path, dest_image_path
    end
  else
    write_log "iso image '#{iso_name}' not available, need to be downloaded"

    if File.exists? base_image_path and File.existss? base_image_copying_lock
      # already being downloaded by another process, wait for it
      write_log "download already started by another process, wait for it"
      while File.exists? base_image_copying_lock
        sleep 1
      end
      write_log "detected iso image download finished"
      prepare_iso_image storage_server, image_pool_dir, vm_dir, iso_name
    else
      # base image does not exist, create downloading lock, and download it
      write_log "start downloading '#{iso_name}'"

      # create download lock
      File.open(base_image_copying_lock, "w") {|f|}
      write_log "created copying lock"

      # TODO use lftp.log to detect whether download succeeded
      lftp_script_file = File.join image_pool_dir, "#{iso_name}.lftp"
      File.open(lftp_script_file, "w") do |f|
        f.write <<LFTP_SCRIPT_FILE
set net:timeout 10
set net:max-retries 2
set net:reconnect-interval-base 1
open #{storage_server}
cd vdisks
get #{iso_name}
LFTP_SCRIPT_FILE
      end
      write_log "generated lftp download script"
      write_log "calling lftp to do download work"
      my_exec "cd #{image_pool_dir} && lftp -f #{iso_name}.lftp 2>&1 >> #{iso_name}.lftp.log"
      write_log "lftp process finished"

      FileUtils.rm_f lftp_script_file
      write_log "removed generated lftp download script"

      # TODO use lftp log to detect whether download succeeded

      FileUtils.rm_f base_image_copying_lock
      write_log "removed copying lock"

      write_log "finished downloading iso image"
    end
    write_log "retry preparing process"
    prepare_iso_image storage_server, image_pool_dir, vm_dir, iso_name

  end
end


# Prepare agent packages. Download packages into package_pool, and soft link them into VM's running dir, so that
# they could be directly used to create iso images.
#
# Authro::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3
def prepare_agent_package storage_server, package_pool, vm_dir, package_name = ""
  # make sure the package pool exists
  FileUtils.mkdir_p package_pool unless File.exists? package_pool

  package_dir = File.join package_pool, package_name
  copying_lock = package_dir + ".copying"
  dest_dir = File.join vm_dir, "agent-cd/packages", package_name

  unless File.exists? "#{vm_dir}/agent-cd/packages"
    FileUtils.mkdir_p "#{vm_dir}/agent-cd/packages"
    write_log "create dir: #{vm_dir}/agent-cd/packages"
  else
    write_log "folder already exists: #{vm_dir}/agent-cd/packages"
  end

  return if package_name == ""

  if File.exists? package_dir and (File.exists? copying_lock) == false
    # package already available
    write_log "package '#{package_name}' already available, making soft link"
    unless File.exists? dest_dir
      FileUtils.ln_s package_dir, dest_dir
    end
  else
    write_log "package '#{package_name}' not available, need to be downloaded"

    if File.exists? package_dir and File.exists? copying_lock
      # already started downloading by another process, wait for it
      write_log "download already started by another process, waiting for it"
      while File.exists? copying_lock
        sleep 1
      end
      write_log "detected download finished, restart preparing procedure"
      prepare_agent_package storage_server, package_pool, vm_dir, package_name
    else
      # package not downloaded, create downloading lock, and download it
      write_log "start downloading '#{package_name}'"

      # create downloading lock
      File.open(copying_lock, "w") {|f|}
      write_log "created downloading lock"

      # use lftp to download packages
      lftp_script_file = File.join package_pool, "#{package_name}.lftp"
      File.open(lftp_script_file, "w") do |f|
        f.write <<LFTP_SCRIPT_FILE
set net:timeout 10
set net:max-retries 2
set net:reconnect-interval-base 1
open #{storage_server}
cd agent_packages
mirror #{package_name} #{package_name}
LFTP_SCRIPT_FILE
      end
      write_log "generated lftp download script"
      write_log "calling lftp to do download work"
      my_exec "cd #{package_pool} && lftp -f #{package_name}.lftp 2>&1 >> #{package_name}.lftp.log"
      write_log "lftp process finished"

      FileUtils.rm_f lftp_script_file
      write_log "removed generated lftp download script"

      FileUtils.rm_f copying_lock
      write_log "removed copying lock file"
      write_log "finished downloading package '#{package_name}'"
    end
    write_log "retry preparing precess"
    prepare_agent_package storage_server, package_pool, vm_dir, package_name
  end
end 


###############################################################################################
#                                                                                             #
#                                        MAIN SECTION                                         #
#                                                                                             #
###############################################################################################


def do_prepare rails_root, storage_server, vm_dir
  puts "preparing"
  image_pool_dir = File.join vm_dir, "../../image_pool"
  package_pool_dir = File.join vm_dir, "../../package_pool"

  File.open "required_images" do |f|
    f.each_line do |line|
      img = line.strip
      if img.end_with? ".qcow2"
        write_log "preparing qcow2 image '#{img}'"
        prepare_hda_image storage_server, image_pool_dir, vm_dir, img
      elsif img.end_with? ".iso"
        write_log "preparing iso image '#{img}'"
        prepare_iso_image storage_server, image_pool_dir, vm_dir, img
      else
        write_log "[warning] don't know how to prepare image '#{img}'!"
      end
    end
  end


  # TODO check if download success

  if File.exists? "agent_packages" or File.exists? "nodelist"
    write_log "preparing required packages"

    # just create the folders (package name not provided)
    prepare_agent_package storage_server, package_pool_dir, vm_dir
    File.read("agent_packages").each_line do |line|
      pkg = line.strip
      prepare_agent_package storage_server, package_pool_dir, vm_dir, pkg
    end

    begin
      write_log "creating agent cd using iso generator"

      igen = CeilIsoGenerator.new
      write_log "config_essential: #{rails_root}/../common/lib/ceil"
      igen.config_essential("#{rails_root}/../common/lib/ceil")

      agent_ip = ""
      agent_submask = ""
      agent_gateway = ""
      agent_dns = ""
      cluster_name = ""
      File.read("agent_hint").each_line do |line|
        line = line.strip
        if line.start_with? "ip="
          agent_ip = line[3..-1]
        elsif line.start_with? "subnet_mask="
          agent_submask = line[12..-1]
        elsif line.start_with? "gateway="
          agent_gateway = line[8..-1]
        elsif line.start_with? "dns="
          agent_dns = line[4..-1]
        elsif line.start_with? "cluster_name="
          cluster_name = line[13..-1]
        end
      end

      write_log "config_network: ip=#{agent_ip}, submask=#{agent_submask}, gateway=#{agent_gateway}, dns=#{agent_dns}, cluster_name=#{cluster_name}"
      igen.config_network(agent_ip, agent_submask, agent_gateway, agent_dns)

      # setup cluster name, so the agent could retrieve keys
      vm_name = File.basename vm_dir

      # note that, the name used here must agree with nodelist file
      node_name = ""
      File.read("nodelist").each_line do |line|
        splt = line.split
        next if splt.length != 2
        if splt[0] == agent_ip
          node_name = splt[1]
          break
        end
      end

      if node_name == ""
        write_log "error: ip address not found in node list!"
      end

      write_log "config_cluster: #{node_name}, #{cluster_name}"
      igen.config_cluster(node_name, cluster_name)

      # set user name, password, etc. and read server info from "storage_server" variable
      server_uri = URI::parse storage_server
      server_user_pwd_host = "#{server_uri.user}:#{server_uri.password}@#{server_uri.host}"
      igen.config_package_server(server_user_pwd_host, server_uri.port.to_s, server_uri.scheme)
      # set key server
      igen.config_key_server(server_user_pwd_host, server_uri.port.to_s, server_uri.scheme)

      igen.config_nodelist(File.read "nodelist")

      # set the software packages
      software_packages = ["common", "ssh-nopass"] # by default, install those 2 packages
      File.read("agent_packages").each_line do |pkg|
        pkg = pkg.strip
        unless software_packages.include? pkg
          software_packages << pkg
        end
      end
      write_log "config_softlist: #{software_packages.join " "}"
      igen.config_softlist(software_packages.join " ")

      igen.generate("#{vm_dir}/agent-cd", "#{vm_dir}/agent-cd.iso")
      unless File.exists? "#{vm_dir}/agent-cd.iso"
        write_log "Failed to create agent-cd.iso!"
      end
    rescue Exception => e
      write_log "Exception: #{e.to_s}"
    end
  end

  # TODO boot vm, handle failure if necessary

  write_log "starting vmachine"
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  uuid = xml_desc["uuid"][0]
  virt_conn = Libvirt::open("qemu:///system")

  begin
    dom = virt_conn.lookup_domain_by_uuid(uuid)
    dom.create
    File.open("status", "w") do |f|
      f.write "using"
    end
    write_log "changed vmachine status to 'using'"

    File.open("xml_desc.real.xml", "w") do |f|
      f.write dom.xml_desc
    end
    write_log "dumped real xml description to 'xml_desc.real.xml'"
  rescue
    write_log "failed to boot vmachine, check the error logs!"
    File.open("status", "w") do |f|
      f.write "destroyed"
    end
    exit  # exit this polling round, no need to do saving. on next calling round, "cleanup" actions will be performed
  end
end


def do_save storage_server, vm_dir
  if File.exists? "hda_save_to"
    write_log "changed VM status to 'saving'"
    File.open("status", "w") do |f|
      f.write "saving"
    end

    hda_image = ""
    File.open("params") do |f|
      f.each_line do |line|
        line = line.strip
        if line.start_with? "hda_image="
          hda_image = line[10..-1]
        end
      end
    end
    # save image file
    File.open("uploading_lock", "w") do |f|
      f.write Time.now
    end
    write_log "uploading_lock created"
    File.open("hda_save_to.lftp", "w") do |f|
      f.write <<HDA_SAVE_TO_LFTP
set net:timeout 10
set net:max-retries 2
set net:reconnect-interval-base 1
open #{storage_server}
put #{hda_image} -o #{File.read "hda_save_to"}
HDA_SAVE_TO_LFTP
    end
    my_exec "lftp -f hda_save_to.lftp 2>&1 >> hda_save_to.log"
    FileUtils.rm_f "uploading_lock"
    FileUtils.rm_f "hda_save_to.lftp"
    write_log "uploading_lock removed"
    write_log "saving done"
  else
    # hda_save_to not found, no need to save, do nothing
    write_log "'hda_save_to' not found, saving skipped"
  end

  write_log "changed VM status to 'destroyed'"
  File.open("status", "w") do |f|
    f.write "destroyed"
  end
end



def do_poll storage_server, vm_dir
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  uuid = xml_desc["uuid"][0]

  virt_conn = Libvirt::open("qemu:///system")
  begin
    dom = virt_conn.lookup_domain_by_uuid(uuid)

    if dom.info.state == LIBVIRT_RUNNING or dom.info.state == LIBVIRT_SUSPENDED
      return  # the vm is still running, skip the following actions
    else
      write_log "detected VM shutdown, saving it"
      begin
        dom.destroy
      ensure
        dom.undefine rescue nil
      end
      do_save storage_server, vm_dir
    end

  rescue
    write_log "failed to find domain while polling, saving the VM before destoying it"
    do_save storage_server, vm_dir
  end
end



def do_cleanup storage_server, vm_dir
  write_log "doing cleanup work"

  if File.exists? "xml_desc.xml"
    # if the vm has xml_desc.xml, we could retrieve the uuid & name, and could archive the running info
    xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
    uuid = xml_desc["uuid"][0]
    name = xml_desc["name"][0]

    # remove big image files
    if File.exists? "required_images"
      File.open "required_images" do |f|
        f.each_line do |line|
          img = line.strip
          if File.exists? img
            FileUtils.rm_f img
          end
        end
      end
      if File.exists? "agent-cd.iso"
        FileUtils.rm_f "agent-cd.iso"
      end
    end

    # make sure the vm domain is destroyed
    virt_conn = Libvirt::open("qemu:///system")
    dom = virt_conn.lookup_domain_by_uuid(uuid) rescue nil
    dom.destroy rescue nil
    dom.undefine rescue nil

    # make sure archive folder exists
    FileUtils.mkdir_p "../../vm_archive"

    parent_dir = File.join vm_dir, ".."
    Dir.chdir parent_dir
    FileUtils.rm_rf "../vm_archive/#{name}.#{uuid}"
    FileUtils.mv vm_dir, "../vm_archive/#{name}.#{uuid}"
  else
    # delete everything, since we don't have any info to archive the VM
    parent_dir = File.join vm_dir, ".."
    Dir.chdir parent_dir
    puts "removing vm folder #{vm_dir}"
    FileUtils.rm_rf vm_dir
  end

end


###############################################################################################
#                                                                                             #
#                                        SCRIPT ENTRY                                         #
#                                                                                             #
###############################################################################################


if ARGV.length < 3
  puts "usage: vm_daemon_helper.rb <rails_root> <storage_server> <vm_dir> <action>"
  exit 1
end

rails_root = ARGV[0]
storage_server = ARGV[1]
vm_dir = ARGV[2]
action = ARGV[3]

Dir.chdir vm_dir

case action
when "prepare"
  do_prepare rails_root, storage_server, vm_dir
when "poll"
  do_poll storage_server, vm_dir
when "save"
  do_save storage_server, vm_dir
when "cleanup"
  do_cleanup storage_server, vm_dir
else
  puts "error: action '#{action}' not understood!"
end

