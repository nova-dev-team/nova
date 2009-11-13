#!/usr/bin/ruby

require 'pathname'
require 'socket'
require File.dirname(__FILE__) + "/utils.rb"

if ARGV.length == 0
  puts "This is installer client for Nova platform"
  puts "usage:"
  puts "  install_client.rb <server_ip:server_port>"
  puts "    -- install with a server, and spread to other nodes"
  puts ""
  puts "  install_client.rb --no-server"
  puts "    -- install with out server"
  exit 1
end

def do_install
  unless File.exist? nova_conf["install_folder"] and Pathname.new(NOVA_ROOT).realpath == Pathname.new(nova_conf["install_folder"]).realpath
    source_files = NOVA_ROOT + "/*"
    sys_exec "mkdir -p #{nova_conf["install_folder"]}"
    sys_exec "cp -r #{source_files} #{nova_conf["install_folder"]}"
  end
  
  if this_node["role"] == "master"
    # TODO trigger first run, in "install folder"!
  elsif this_node["role"] == "worker"
    # TODO trigger first run, in "install folder"!
  end

end

if ARGV[0] == "--no-server"
  do_install
  exit 0
end

splt = ARGV[0].split ":"
server_ip, server_port = splt[0], splt[1].to_i

TCPSocket.open(server_ip, server_port) do |sock|
  # do installation
  sock.write "install started\n"

  do_install

  sock.write "install finished\n"
  # after installation

  loop do
    sock.write "get_client\n"
    line = sock.readline
    break if line == "no more client"
# TODO configure ssh-nopass before running this script
    sleep rand(10)
  end
end

