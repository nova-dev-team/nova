#!/usr/bin/ruby

require 'libvirt'
require 'fileutils'
require 'xmlsimple'

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
  # if iso exists in the image_pool
  #   make hard link, directly use it
  # else there it is not in image_pool
  #   if the base image is being copied, wait until it is ready, and make hard link
  #   else the base image is not copied, download it directly, and make hard link

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

if ARGV.length < 3
  puts "usage: vm_daemon <storage_server> <vm_dir> <action>"
  exit 1
end

storage_server = ARGV[0]
vm_dir = ARGV[1]
action = ARGV[2]

Dir.chdir vm_dir

case action
when "prepare"
  puts "preparing"
  image_pool_dir = File.join vm_dir, "../../image_pool"

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

  # TODO make agent iso images

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

when "poll"
  xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
  uuid = xml_desc["uuid"][0]

  virt_conn = Libvirt::open("qemu:///system")
  begin
    dom = virt_conn.lookup_domain_by_uuid(uuid)

    if dom.info.state == LIBVIRT_RUNNING or dom.info.state == LIBVIRT_SUSPENDED
      exit  # the vm is still running, skip the following actions
    else
      begin
        dom.destroy
      ensure
        begin
          dom.undefine
        rescue
        end
      end
    end

  rescue
    # domain not found, write "saving" to status file
    File.open("status", "w") do |f|
      f.write "saving"
    end

    # XXX don't exit here. need to save image int the following code
  end

  if File.exists? "hda_save_to"

    write_log "detected vmachine stopped, changed status to 'saving'"

    # vm stopped, start saving job
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
    puts "uploading_lock created"
    File.open("hda_save_to.lftp", "w") do |f|
      f.write <<HDA_SAVE_TO_LFTP
set net:max-retries 2
set net:reconnect-interval-base 1
open #{storage_server}
put #{hda_image} -o #{File.read "hda_save_to"}
HDA_SAVE_TO_LFTP
    end
    my_exec "lftp -f hda_save_to.lftp 2>&1 >> hda_save_to.log"
    FileUtils.rm "uploading_lock"
    puts "uploading_lock removed"
    
  else
    # hda_save_to not found, no need to save, do nothing
    write_log "detected vmachine stopped, saving not required"
  end

  # when saving finished, make the vm as 'destroyed'
  File.open("status", "w") do |f|
    f.write "destroyed"
  end

  write_log "changed vmachine status to 'destroyed'"

when "cleanup"
  puts "doing cleanup"
  write_log "detected vmachine destroyed"

  if File.exists? "xml_desc.xml"
    # if the vm has xml_desc.xml, we could retrieve the uuid & name, and could archive the running info

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
    end

    # make sure the vm domain is destroyed
    virt_conn = Libvirt::open("qemu:///system")
    begin
      dom = virt_conn.lookup_domain_by_uuid(uuid)

      if dom.info.state == LIBVIRT_RUNNING or dom.info.state == LIBVIRT_SUSPENDED
        exit  # the vm is still running, skip the following actions
      else
        begin
          dom.destroy
        ensure
          begin
            dom.undefine
          rescue
          end
        end
      end

    rescue
      # domain not found, write "saving" to status file
      File.open("status", "w") do |f|
        f.write "saving"
      end

      # XXX don't exit here. need to save image int the following code
    end

    # save info files into archive
    xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
    uuid = xml_desc["uuid"][0]
    name = xml_desc["name"][0]
    FileUtils.mkdir_p "../../vm_archive"

    parent_dir = File.join vm_dir, ".."
    Dir.chdir parent_dir
    FileUtils.mv vm_dir, "../vm_archive/#{name}.#{uuid}"
  else
    # delete everything
    parent_dir = File.join vm_dir, ".."
    Dir.chdir parent_dir
    puts "removing vm folder #{vm_dir}"
    FileUtils.rm_rf vm_dir
  end

else
  puts "error: action '#{action}' not understood!"
end

