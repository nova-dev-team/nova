#!/usr/bin/ruby

require 'rubygems'
require 'libvirt'
require 'fileutils'
require "#{File.dirname __FILE__}/utils"
require 'xmlsimple'

SCAN_INTERVAL = 60 #every 60 min
if ARGV.length < 2
  puts "usage: vm_dir_cleaner <rails_root> <run_root>"
  exit 1
end

RAILS_ROOT = ARGV[0]
RUN_ROOT = ARGV[1]
VM_ROOT = File.join(RUN_ROOT, "vm")

WORKER_UUID = File.read(File.join(RAILS_ROOT, "config", "worker.uuid"))
LOGFILE = File.join(RAILS_ROOT, "log", "vm_dir_cleaner.log")
PIDFILE = File.join(RAILS_ROOT, "log", "vm_dir_cleaner.pid")

#logic
#check all dir in vm_root
#  check host.uuid, if not exists, delete!
#  compare host.uuid to worker.uuid, it not equal, ignore it
#  if equals, check the vm in libvirt, if not exists, delele!
#

def write_log message
  puts message
  File.open(LOGFILE, "a") do |f|
    message.each_line do |line|
      if line.end_with? "\n"
        f.write "[#{Time.now}] #{line}"
      else
        f.write "[#{Time.now}] #{line}\n"
      end
    end
  end
end

def cleanup vm_dir, reason
  write_log "removing useless vm_dir #{vm_dir}, because #{reason}"
  FileUtils.rm_rf vm_dir
end


begin
  File.open(PIDFILE, "w") do |f|
    f.write Process.pid
  end
rescue
  puts "cannot write pid file #{PIDFILE}!"
  exit 1
end

while true
  Dir.foreach(VM_ROOT) do |vm_entry|
    begin
      vm_dir_path = File.join VM_ROOT, vm_entry
      vm_xml_fn = File.join vm_dir_path, "xml_desc.xml"
      host_uuid_fn = File.join vm_dir_path, "host.uuid"
      next if vm_entry.start_with? "."
      next unless File.directory? vm_dir_path

      host_uuid = nil
      sleep 1
      begin
        host_uuid = File.read(host_uuid_fn)
      rescue
        host_uuid = nil
      end

      if host_uuid
        if host_uuid == WORKER_UUID
          uuid = nil
          begin
            xml_desc = XmlSimple.xml_in(File.read(vm_xml_fn))
            uuid = xml_desc["uuid"][0]
          rescue
            uuid = nil
          end

          if uuid
            conn = Libvirt::open("xen:///") #only for xen+nfs
            dom = nil
            begin
              dom = conn.lookup_domain_by_uuid(uuid)
            rescue
              dom = nil
            end

            if dom == nil
              #cleanup(vm_dir_path, "vmachine not defined")
            end
            conn.close
          else
            cleanup(vm_dir_path, "xml_desc.xml is broken")
          end

        end
      else
        cleanup(vm_dir_path, "host.uuid not found")
      end

    rescue
    end
  end

  sleep 60 * SCAN_INTERVAL
end



