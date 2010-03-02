#!/usr/bin/ruby

require 'pathname'
require 'socket'
require File.dirname(__FILE__) + "/utils.rb"

if ARGV.length == 0
  puts "This is installer client for Nova platform"
  puts "usage:"
  puts "  install_client.rb --install-server=server_ip:server_port"
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

def install_client node_ip, install_server
  cmd = "ssh -o stricthostkeychecking=no #{node_ip} 'mkdir -p #{nova_conf["install_folder"]}'"
  sys_exec cmd
  cmd = "scp -o stricthostkeychecking=no -r ../* #{node_ip}:/#{nova_conf["install_folder"]}"
  sys_exec cmd
  cmd = "ssh -o stricthostkeychecking=no #{node_ip} 'cd #{nova_conf["install_folder"]}/scripts && bash install.sh --single-node --install-server=#{install_server}'"
  sys_exec cmd
end

server_ip = nil
server_port = -1

ARGV.each do |arg|
  if arg == "--no-server"
    puts "Installing without install server"
    do_install
    exit 0
  elsif arg.start_with? "--install-server="
    splt = arg.split "="
    splt = splt[1].split ":"
    server_ip, server_port = splt[0], splt[1].to_i
    puts "Installing with install server at #{server_ip}:#{server_port}"
  end
end

TCPSocket.open(server_ip, server_port) do |sock|
  # do installation
  sock.write "install_started on node #{this_node["intranet_ip"]}\n"

  do_install

  sock.write "install_finished on node #{this_node["intranet_ip"]}\n"

  loop do
    begin
      sock.write "get_client request from node #{this_node["intranet_ip"]}\n"
      line = sock.readline.chomp
      break if line.start_with? "no_more_client"
      # TODO configure ssh-nopass before running this script
      if line.start_with? "install"
        splt = line.split
        install_client splt[1], "#{server_ip}:#{server_port}"
      end
      sleep rand
    rescue
      break
    end
  end
end

