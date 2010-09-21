#!/usr/bin/ruby

require 'rubygems'
require 'posixlock'
require 'libvirt'
require 'fileutils'
require 'xmlsimple'
require 'ceil_iso_generator'
require 'utils'
require 'uri'

# libvirt status constants
LIBVIRT_RUNNING = 1
LIBVIRT_SUSPENDED = 3
LIBVIRT_NOT_RUNNING = 5

COMMON_CONF = common_conf
HYPERVISOR = COMMON_CONF["hypervisor"]
STORAGE_TYPE = COMMON_CONF["storage_type"]

#unused in current version
#0.31
MIGRATION_TIMEOUT_HANDSHAKE=5
# 5min timeout for handshake
MIGRATION_TIMEOUT_PROCEEDING=-1

$libvirt_connection = nil
#


###################################INIT##############################################
if ARGV.length < 4
  puts "usage: vm_daemon_helper.rb <rails_root> <storage_server> <vm_dir> <vm_name>"
  exit 1
end

RAILS_ROOT = ARGV[0]
STORAGE_SERVER = ARGV[1]
VM_DIR = ARGV[2]
VM_NAME = ARGV[3]

pid = Process.pid

#try to gain lock vm_dir/vm_daemon.pid
#if failed, exits

pid_fn = File.join(VM_DIR, 'vm_daemon.pid')
pid_file = File.new(pid_fn, "w+")

locked = pid_file.posixlock(File::LOCK_EX | File::LOCK_NB)
if locked
  pid_file.write pid
  pid_file.flush
else
  puts "cannot gain file lock of #{pid_fn}, exits"
  exit 1
end

###################################END OF INIT##########################################


def libvirt_connect_local
  if $libvirt_connection == nil
    case HYPERVISOR
    when "xen"
      $libvirt_connection = Libvirt::open("xen:///")
    when "kvm"
      $libvirt_connection = Libvirt::open("qemu:///system")
    else
      raise "vm_daemon: unsupported hypervisor: #{HYPERVISOR}."
    end
  end
  return $libvirt_connection
end


def write_log message
  File.open(File.join(VM_DIR, "log"), "a") do |f|
    message.each_line do |line|
      if line.end_with? "\n"
        f.write "[#{Time.now}][#{VM_NAME}] #{line}"
      else
        f.write "[#{Time.now}][#{VM_NAME}] #{line}\n"
      end
    end
  end
end


def my_exec cmd
  write_log "[cmd] #{cmd}"
  system cmd
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


def prepare_hda_image_directly image_pool_dir, vm_dir, hda_name
  #simply move image_poor_dir/hda_name to vm_dir
  base_image_name = File.join image_pool_dir, hda_name
  base_image_copying_lock = File.join image_pool_dir, "#{hda_name}.copying"
  if File.exists? base_image_name and (File.exists? base_image_copying_lock) == false
    my_exec "mv #{base_image_name} #{File.join vm_dir, hda_name}"
  else
    write_log "image #{hda_name} is not prepared, cannot use it"
  end
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
    write_log "hda image is not ready!"

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
    return false
    # retry in outer loop
#    write_log "retry preparing process"
#    prepare_hda_image storage_server, image_pool_dir, vm_dir, hda_name
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

  begin
    File.open(File.join(vm_dir, "status"), "w") do |f|
      f.write "preparing"
    end
  rescue

  end

  image_pool_dir = File.join vm_dir, "../../image_pool"
  package_pool_dir = File.join vm_dir, "../../package_pool"

  File.open(File.join(vm_dir, "required_images")) do |f|
    f.each_line do |line|
      img = line.strip
      img_fn = File.join vm_dir, img
      retry_count = 5

      while retry_count > 0
        if img.end_with? ".qcow2"
          write_log "preparing qcow2 image '#{img}'"
          prepare_hda_image storage_server, image_pool_dir, vm_dir, img
        elsif img.end_with? ".qcow"
          write_log "preparing qcow image '#{img}'"
          prepare_hda_image storage_server, image_pool_dir, vm_dir, img
        elsif img.end_with? ".iso"
          write_log "preparing iso image '#{img}'"
          prepare_iso_image storage_server, image_pool_dir, vm_dir, img
        elsif img.end_with? ".img"
          write_log "preparing img image '#{img}'"
          prepare_hda_image storage_server, image_pool_dir, vm_dir, img
        else
          write_log "directly use image file '#{img}'"
          prepare_hda_image_directly image_pool_dir, vm_dir, img
        end

        break if File.exists?(img_fn)

        retry_count = retry_count - 1
        interval = rand(10) + 1
        sleep interval
      end
    end
  end

  write_log "image has been prepared"

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
      id_rsa = nil
      id_rsa_pub = nil

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
        elsif line.start_with? "id_rsa="
          id_rsa = line[7..-1]
        elsif line.start_with? "id_rsa_pub="
          id_rsa_pub = line[11..-1]
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

      #set ssh key
      if id_rsa and id_rsa_pub
        igen.config_ssh_key(id_rsa, id_rsa_pub)
      end

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

  virt_conn = libvirt_connect_local
  # connect to local libvirtd

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

def do_restart vm_dir
  # restart
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  vm_uuid = xml_desc["uuid"][0]
  write_log "restarting vmachine #{vm_uuid}"
  system "virsh create xml_desc.xml"
=begin
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  vm_uuid = xml_desc["uuid"][0]
  virt_conn = libvirt_connect_local
  dom = virt_conn.lookup_domain_by_uuid(vm_uuid)
  if dom.info.state == LIBVIRT_NOT_RUNNING
    write_log "restarting vmachine #{vm_uuid}"
    dom.create
  end
=end
end

def do_save storage_server, vm_dir
  begin
  rescue
  end

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


# when a incoming migration occurs, worker would run vm_daemon RECEIVE first
# then call to here
# migration can be done in 3 steps
# 1. handshake, src worker put file 'migrate_request' in vm_dir, and waiting dst worker put file 'migrate_accept'
#      if time out, migration fail
#      just exit, src worker will also timeout so migration ends
# 2. wait for remote worker finish
#      when finish, src worker removes file 'migrate_to'
#      master should handle timeout here
#      complicated situation..
#      what happens when xen live migration fails?
# 3. remove 'migrate_request' and 'migrate_accept'
#      migration complete

def do_receive storage_server, vm_dir
  #1.handshake
  #waiting for migrate_request

  timeout = MIGRATION_TIMEOUT_HANDSHAKE * 10
  while timeout != 0
    break if File.exists? "migrate_request"
    timeout = timeout - 1
    sleep 6 #check it every 6 sec
  end
  return false if timeout == 0

  begin
    File.open("migrate_accept", "w") do |f|
      f.write "blah"  #can write some useful message?
    end
  rescue
    # fuckingly cannot write file, handshake fail, exit
    return false
  end

  #2.when src worker see migrate_accept, it will call libvirt::migrate
  #  after migration finished, file 'migrate_to' will be removed
  #  spin wait for 'migrate_to' disappear

  timeout = MIGRATION_TIMEOUT_PROCEEDING * 10
  while timeout != 0
    if File.exists? "migrate_to"
    else
      break
    end
    timeout = timeout - 1
    sleep 6
  end

  # 3.yes you got it
  #   remove 'migrate_request' & 'migrate_accept'
  FileUtils.rm_f "migrate_accept"
  FileUtils.rm_f "migrate_request"
  return true
end


def do_migrate
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  vm_uuid = xml_desc["uuid"][0]
  virt_conn = libvirt_connect_local
  dom = virt_conn.lookup_domain_by_uuid(vm_uuid)

  if dom.info.state == LIBVIRT_NOT_RUNNING
    write_log "vm #{vm_uuid} not running! cannot live migrate!"
    return -1
  end

  migrate_dest = File.read "migrate_to"
  if migrate_dest && migrate_dest.length > 0
    write_log "now migrate vm<#{vm_uuid}> to worker '#{migrate_dest}'"
    old_status = File.read "status"

    File.open("status", "w") do |f|
      f.write "migrating"
    end

    write_log "changed VM status to 'migrating'"
    begin
      cmd = "virsh migrate --live " + vm_uuid + " xen:/// xenmigr://" + migrate_dest + " 2>&1 >> migration.log"
      result = my_exec cmd

      if result
        write_log "migrating success!"
        File.open("status", "w") do |f|
          f.write "using"
        end
      else
        write_log "migrating maybe failed! result = #{result}"
        File.open("status", "w") do |f|
          f.write "#{old_status}"
        end
      end

      #check local dom running status
      #if running, migrate failed/migrate to self success, just skip
      #if not running, undefine it, then exit

      begin
        dom = virt_conn.lookup_domain_by_uuid(vm_uuid)
        if dom.info.state == LIBVIRT_NOT_RUNNING
          write_log "cleaning #{vm_uuid} on source worker!"
          dom.destroy rescue nil
          dom.undefine rescue nil
        end
        exit 0
      rescue => e
        write_log "[migration]: #{e.to_s}"
      end

    rescue
      write_log "call to live migrate failed!"
      File.open("status", "w") do |f|
        f.write "#{old_status}"
      end
    end
    write_log "migrating finished, remove migrating tag"
    FileUtils.rm_f "migrate_to"
  else
    write_log "invalid params, migrating failed"
    FileUtils.rm_f "migrate_to"
    #invalid params
  end
end


def do_poll storage_server, vm_dir
=begin
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  uuid = xml_desc["uuid"][0]

  virt_conn = libvirt_connect_local
  begin
    dom = virt_conn.lookup_domain_by_uuid(uuid)

    if dom.info.state != LIBVIRT_NOT_RUNNING
      return
      # the vm is still running, skip the following actions
    else
      # write_log "detected VM shutdown, saving it"
      #begin
      #  dom.destroy
      #ensure
      #  dom.undefine rescue nil
      #end
      #do_save storage_server, vm_dir
    end
  rescue
    #write_log "failed to find domain while polling, saving the VM before destoying it"
    #do_save storage_server, vm_dir
  end
=end
end


def do_add_pkg vm_dir
  #1.get vm's ip address
  agent_ip = nil
  File.read("agent_hint").each_line do |line|
    line = line.strip
    if line.start_with? "ip="
      agent_ip = line[3..-1]
    end
  end
  if agent_ip == nil
    write_log "[add_pkg]cannot get vm's ip_addr!"
    return
  end

  #2.get ceil password
  agent_password = nil
  begin
    agent_password = File.read("ceil_password")
  rescue
  end

  if agent_password == nil
    write_log "[add_pkg]dont know vm's ceil_password!"
    return
  end

  #3.get pkg_list
  pkg_list = nil
  begin
    pkg_list = File.read("pkg_list")
  rescue
  end
  if pkg_list == nil
    write_log "[add_pkg]cannot get vm's package_list"
    return
  end

  #4.get package server addr
  pkg_server_addr = nil
  begin
    pkg_server_addr = File.read("pkg_server")
  rescue
  end
  if pkg_server_addr == nil
    write_log "[add_pkg]dont know package_server's url"
    return
  end

  #4.connect-> send password, send "addjob", send ftp://server/pkg_list
  require 'ceil_caller'
  caller = CeilCaller.new(agent_ip, agent_password, pkg_server_addr)

  for pkg_list.each_line do |app_name|
    result = caller.add_job(app_name)
    if result == nil
      write_log "[add_pkg]cannot connect to ceil_agent on #{agent_ip}!"
    end
  end
  #5.ok!
end


def do_resume vm_dir
  #suspend the vm running in vm_dir
  Dir.chdir vm_dir
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  uuid = xml_desc["uuid"][0]

  virt_conn = libvirt_connect_local
  begin
    dom = virt_conn.lookup_domain_by_uuid(uuid)
    dom.resume
  rescue => e
    write_log "error while resuming vmachine #{uuid}, message: #{e.to_s}"
  end
end


def do_suspend vm_dir
  #suspend the vm running in vm_dir
  Dir.chdir vm_dir
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  uuid = xml_desc["uuid"][0]

  virt_conn = libvirt_connect_local
  begin
    dom = virt_conn.lookup_domain_by_uuid(uuid)
    dom.suspend
  rescue => e
    write_log "error while suspending vmachine #{uuid}, message: #{e.to_s}"
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
    virt_conn = libvirt_connect_local
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


def get_action
  Dir.chdir VM_DIR
  action = nil
  action = File.read "action" rescue nil

  if action
    begin
      FileUtils.rm_f "action"
    rescue
      #cannot remove instruction file, for safety, we do nothing
      return "poll"
    end
    return action
  else
    return "poll"
  end

end

def do_action action
  rails_root = RAILS_ROOT
  storage_server = STORAGE_SERVER
  vm_dir = VM_DIR


  begin
    case action
    when "prepare"
      write_log "vm_daemon action: prepare"
      do_prepare rails_root, storage_server, vm_dir
    when "receive"
      write_log "vm_daemon action: receive"
      do_receive storage_server, vm_dir
    when "migrate"
      write_log "vm_daemon action: migrate"
      do_migrate
    when "poll"
      #write_log "vm_daemon_helper action: poll"
      do_poll storage_server, vm_dir
    when "save"
      write_log "vm_daemon action: save"
      do_save storage_server, vm_dir
    when "cleanup", "destroy"
      write_log "vm_daemon action: #{action}"
      do_cleanup storage_server, vm_dir
    when "restart"
      write_log "vm_daemon action: restart"
      do_restart vm_dir
    when "suspend"
      write_log "vm_daemon action: suspend"
      do_suspend vm_dir
    when "resume"
      write_log "vm_daemon action: resume"
      do_resume vm_dir
    when "add_pkg" #this is for realtime on demand software deployment
      write_log "vm_daemon action: add_pkg"
      do_add_pkg vm_dir
    else
      write_log "error: action '#{action}' not understood!"
    end
  rescue => e
    if action != "poll"
      write_log "vm_daemon error: #{e.to_s}"
    end
  end
end


#compare vm's host uuid to local uuid
#equal -> do action
#not equal -> fix it(vm running on local can be seen in libvirt) | undefine(vm shuted-off)

def check_vm_host_uuid
  begin
    worker_uuid_fn = File.join RAILS_ROOT, 'config', 'worker.uuid'
    vm_worker_uuid_fn = File.join VM_DIR, 'host.uuid'

    #puts worker_uuid_fn
    #puts vm_worker_uuid_fn

    host_uuid = File.read(worker_uuid_fn)
    vm_host_uuid = File.read(vm_worker_uuid_fn)

    # if vm is running on local, fix host.uuid
    # this is caused by migration
    # if vm is shut-off, kill it in 'do_poll'
    #puts "host_uuid = #{host_uuid}"
    #puts "vm_host_uuid = #{vm_host_uuid}"

    if host_uuid != vm_host_uuid
      begin
        vm_xml_fn = File.join VM_DIR, "xml_desc.xml"
        xml_desc = XmlSimple.xml_in(File.read vm_xml_fn)
        vm_uuid = xml_desc["uuid"][0]
        virt_conn = libvirt_connect_local
        dom = virt_conn.lookup_domain_by_uuid(uuid)
        if dom.info.state != LIBVIRT_NOT_RUNNING
          File.open(vm_host_uuid, "w") do |f|
            f.write host_uuid
          end
        end
      rescue
      end
    end
  rescue
    write_log "cannot rewrite host.uuid file!!"
    ## couldn't get it, consider a bad machine so trash cleaner will handle it
    ## should exit here
    # exit 1
  end
end


#### main loop ####
# 1.check vm exists in local libvirt
# 2. if exists, do action, then goto 1
# 3. else, do clean up and exits
#

while true
  begin
    check_vm_host_uuid
  rescue => e
    write_log e.to_s
  end

  begin
    virt_conn = libvirt_connect_local

    dom = nil
    begin
      dom = virt_conn.lookup_domain_by_name(VM_NAME)
    rescue
    end

    if dom
      action = get_action
      do_action action
    else
      # do_action "cleanup"
      pid_file.posixlock(File::LOCK_NB | File::LOCK_UN)
      pid_file.close
      exit 0
    end
  rescue => e
    write_log e.to_s
  end
  sleep 3
end

