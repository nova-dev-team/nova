#!/usr/bin/ruby

require 'libvirt'
require 'fileutils'

def my_exec cmd
  puts "[cmd] #{cmd}"
  system cmd
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
  File.open("downloading_lock", "w") do |f|
    f.write Time.now
  end
  puts "downloading_lock created"
  my_exec "lftp -f lftp.retr.script"
  FileUtils.rm "downloading_lock"
  puts "downloading_lock removed"

  # TODO check if download success

  # TODO boot vm, handle failure if necessary
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

  parent_dir = (File.split vm_dir)[0]
  Dir.chdir parent_dir
  
  puts "removing vm folder #{vm_dir}"
  FileUtils.rm_rf vm_dir
else
  puts "error: action '#{action}' not understood!"
end

