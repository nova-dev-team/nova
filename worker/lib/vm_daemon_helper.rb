#!/usr/bin/ruby

require 'libvirt'
require 'fileutils'
require 'xmlsimple'

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

if ARGV.length < 2
  puts "usage: vm_daemon <vm_dir> <action>"
  exit 1
end

vm_dir = ARGV[0]
action = ARGV[1]

Dir.chdir vm_dir

case action
when "prepare"
  puts "preparing"
  write_log "started downloading required resource"
  File.open("downloading_lock", "w") do |f|
    f.write Time.now
  end
  puts "downloading_lock created"
  my_exec "lftp -f lftp.retr.script"
  FileUtils.rm "downloading_lock"
  puts "downloading_lock removed"
  write_log "finished downloading required resource"

  # TODO check if download success

  # TODO make agent iso images

  # TODO boot vm, handle failure if necessary

  write_log "changed vmachine status to 'using'"
when "poll"
  puts "polling"

  # TODO poll vm running status, do "saving" work if necessary

  # start saving job
  File.open("status", "w") do |f|
    f.write "saving"
  end

  # save image file
  File.open("uploading_lock", "w") do |f|
    f.write Time.now
  end
  puts "uploading_lock created"
  my_exec "lftp -f lftp.stor.script"
  FileUtils.rm "uploading_lock"
  puts "uploading_lock removed"
  # TODO handle upload error if necessary


  # when saving finished, make the vm as 'destroyed'
  File.open("status", "w") do |f|
    f.write "destroyed"
  end

when "cleanup"
  puts "doing cleanup"

  # move logs into archive folder, rename it as #{uuid}.#{vm_name}.log
  if File.exists? "xml_desc.xml"
    FileUtils.mkdir_p "../archive"
    xml_desc = XmlSimple.xml_in(File.read "xml_desc.xml")
    uuid = xml_desc["uuid"]
    name = xml_desc["name"]
    FileUtils.mv "log", "../archive/#{uuid}.#{name}.log"
  end

  parent_dir = (File.split vm_dir)[0]
  Dir.chdir parent_dir
  
  puts "removing vm folder #{vm_dir}"
  FileUtils.rm_rf vm_dir
else
  puts "error: action '#{action}' not understood!"
end

