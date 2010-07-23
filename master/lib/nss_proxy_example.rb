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
puts

puts "NSS proxy error message: '#{np.error_message}'"

