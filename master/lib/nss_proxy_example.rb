#!/usr/bin/ruby

require 'rubygems'
require 'pp'
require 'uuidtools'
require File.dirname(__FILE__) + "/nss_proxy.rb"

NSS_ADDR = "192.168.0.147:5000"

np = NssProxy.new NSS_ADDR
puts "Created NSS proxy for '#{NSS_ADDR}'"
puts "NSS proxy status: '#{np.status}'"
puts "NSS proxy error message: '#{np.error_message}'"

puts "--"
puts "Request result for 'listdir':"
pp np.listdir "agent_packages"
puts "NSS proxy error message: '#{np.error_message}'"
puts

puts "--"
puts "Request result for 'listdir':"
pp np.listdir
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'cp':"
pp np.cp "blah.qcow2", "blah.qcow2.a"
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'listdir':"
pp np.listdir
puts "NSS proxy error message: '#{np.error_message}'"
puts

puts "--"
puts "Request result for 'mv':"
pp np.mv "blah.qcow2.a", "blah.qcow2.b"
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'listdir':"
pp np.listdir
puts "NSS proxy error message: '#{np.error_message}'"
puts

puts "--"
puts "Request result for 'rm':"
pp np.rm "blah.qcow2.b"
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'listdir':"
pp np.listdir
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'role':"
pp np.role
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'version':"
pp np.version
puts "NSS proxy error message: '#{np.error_message}'"
puts

puts "--"
puts "Request result for 'hostname':"
pp np.hostname
puts "NSS proxy error message: '#{np.error_message}'"
puts

puts "--"
puts "Request result for 'register_vdisk':"
pp np.unregister_vdisk "blah.qcow2"
puts "NSS proxy error message: '#{np.error_message}'"
puts

puts "--"
puts "Request result for 'register_vdisk':"
pp np.register_vdisk "blah.qcow2", 5
puts "NSS proxy error message: '#{np.error_message}'"
puts


puts "--"
puts "Request result for 'register_vdisk':"
pp np.edit_vdisk "blah.qcow2", 2
puts "NSS proxy error message: '#{np.error_message}'"
puts



