#!/usr/bin/env ruby

require 'dbus'
require 'libvirt'

# You might want to change this
ENV["RAILS_ENV"] ||= "production"

require File.dirname(__FILE__) + "/../../config/environment"

$running = true
Signal.trap("TERM") do 
  $running = false
end

virt_conn = Libvirt::open("qemu:///system")
bus = DBus::SystemBus.instance
vm_service = bus.service("thuhpc.datagrid.nova")
vm_starter = vm_service.object("thuhpc/datagrid/nova/vmstarter")

while($running) do
  
  # Replace this with your code
  ActiveRecord::Base.logger.info "This daemon is still running at #{Time.now}.\n"
  
  sleep 1
end
