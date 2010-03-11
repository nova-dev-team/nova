#!/usr/bin/ruby

require 'libvirt'

if ARGV.length == 0
  puts "usage: vm_daemon <vm_dir>"
  exit 1
end

vm_dir = ARGV[0]

