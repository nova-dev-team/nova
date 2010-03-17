#!/usr/bin/ruby

=begin
require File.join(File.dirname(__FILE__), 'nodelist')

def each_node cmd
  all_node_name do |n|
    puts "=====================================  #{n}  ======================================="
    sys_exec "ssh #{n} '#{cmd}'"
    puts "-------------------------------------  #{n}  ---------------------------------------"
    puts "\n\n"
  end
end

NOVA_ROOT = File.join File.dirname(File.expand_path(__FILE__)), "../"
puts "Working with NOVA_ROOT=#{NOVA_ROOT}"

case ARGV[0]
when "rm_cache" then each_node "rm -r #{NOVA_ROOT}/worker/tmp/work_site/storage_cache"
when "rm_log" then each_node "rm -r #{NOVA_ROOT}/worker/log"
when "first_run" then each_node "cd #{NOVA_ROOT}/worker && ./first_run.sh"
when "start" then each_node "cd #{NOVA_ROOT}/worker && ./start.sh -d"
when "stop" then each_node "cd #{NOVA_ROOT}/worker && ./stop.sh"
when "update" then each_node "cd #{NOVA_ROOT}/worker && git pull"
when "list_vm" then each_node "virsh list"
when "list_cache" then each_node "ls -lh #{NOVA_ROOT}/worker/tmp/work_site/storage_cache"
when "create_br" then
  each_node "ruby #{NOVA_ROOT}/scripts/create_br.rb"
when "install" then
  sys_exec "cd ../installer && ./make_installer.sh && cd .."
  all_node_name do |n|
    sys_exec "scp -r ../installer #{n}:/root"
  end
  each_node "cd /root/installer && ./install.sh"
else
  if ARGV[0] == nil
    puts "usage:"
    puts "Just check the source code!"
  else
    each_node(ARGV.join " ")
  end
end
=end

require 'utils.rb'

def all_node_exec cmd
  all_nodes do |node|
    puts node["intranet_ip"]
  end
end

all_node_exec "hi"
