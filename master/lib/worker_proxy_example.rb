#!/usr/bin/ruby

require 'rubygems'
require 'pp'
require 'uuidtools'
require File.dirname(__FILE__) + "/worker_proxy.rb"

WORKER_ADDR = "166.111.131.10:3004"

wp = WorkerProxy.new WORKER_ADDR
puts "Created worker proxy for '#{WORKER_ADDR}'"
puts "Worker proxy status: '#{wp.status}'"
puts "Worker proxy error message: '#{wp.error_message}'"

puts "--"
puts "Request result for 'list_vm':"
pp wp.list_vm
puts

puts "--"
puts "Request result for 'get_hostname':"
pp wp.get_hostname
puts

puts "--"
puts "Request result for 'get_version':"
pp wp.get_version
puts

puts "--"
puts "Request result for 'get_rails_env':"
pp wp.get_rails_env
puts

puts "--"
puts "Request result for 'revoke_image':"
pp wp.revoke_image "vm_pool_6.qcow2"
puts

puts "--"
puts "Request result for 'revoke_package':"
pp wp.revoke_package "pkg_test"
puts

puts "--"
puts "Request result for 'list_setting':"
pp wp.list_setting
puts

puts "--"
puts "Request result for 'show_setting':"
pp wp.show_setting "run_root"
puts

puts "--"
puts "Request result for 'edit_setting':"
pp wp.edit_setting "run_root", "/blah"
pp wp.edit_setting "image_pool_size", "2"
puts

puts "--"
puts "Suspending all VMs by uuid"
wp.list_vm.each do |vm|
  pp vm
  pp wp.suspend_vm vm["uuid"]
end

puts "--"
puts "Resuming all VMs by name"
wp.list_vm.each do |vm|
  pp vm
  pp wp.resume_vm vm["uuid"]
end

puts "--"
puts "Destroying all VMs by uuid"
wp.list_vm.each do |vm|
  pp vm
  pp wp.destroy_vm vm["uuid"]
end

puts "--"
puts "Trying to start a new vm"
pp wp.start_vm :name => "worker_proxy_test",
  :uuid => UUIDTools::UUID.random_create.to_s,
  :memory_size => 333,
  :cpu_count => 2,
  :vdisk_fname => "ubuntu_ptp8_ceil_enabled.qcow2",
  :ip => "10.0.4.222",
  :submask => "255.255.255.0",
  :gateway => "10.0.4.254",
  :dns => "166.111.8.29",
  :packages => "pkg_test,pkg_test2",
  :nodelist => "10.0.4.222 nodex",
  :cluster_name => "c_test1"


