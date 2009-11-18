#!/usr/bin/ruby

require File.dirname(__FILE__) + "/utils.rb"

def ssh_nopass
    ensure_temp_dir_exists
    all_nodes do |node|
	puts "collecting ssh pubkey from ip: #{node["intranet_ip"]}"
	sub_process = fork {
	    # XXX hardcode: username=root, path=/root/.ssh/id_rsa.pub
	    system "scp #{node["intranet_ip"]}:/root/.ssh/id_rsa.pub #{TEMP_DIR}/#{node["intranet_ip"]}-pubkey"
	}
	Process.waitpid sub_process
	sys_exec "cat #{TEMP_DIR}/#{node["intranet_ip"]}-pubkey >> #{TEMP_DIR}/allnodes-pubkey"
    end
    all_nodes do |node|
	puts "sending authorized key set to ip: #{node["intranet_ip"]}"
	sub_process = fork {
	    system "scp #{TEMP_DIR}/allnodes-pubkey #{node["intranet_ip"]}:/root/.ssh/"
	    system "ssh #{node["intranet_ip"]} 'cat /root/.ssh/allnodes-pubkey >> /root/.ssh/authorized_keys'"
	}
	Process.waitpid sub_process
    end
end

