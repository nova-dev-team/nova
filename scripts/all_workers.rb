#!/usr/bin/ruby

require 'nodelist'

def each_node cmd
    all_node_name do |n|
	puts "=====================================  #{n}  ======================================="
	sys_exec "ssh #{n} '#{cmd}'"
	puts "-------------------------------------  #{n}  ---------------------------------------"
	puts "\n\n"
    end
end

case ARGV[0]
when "clear" then each_node "rm -r /root/v2/worker/tmp/work_site/storage_cache"
when "diskfree" then each_node "df -h"
when "firstrun" then each_node "cd /root/v2/worker && ./first_run.sh -d"
when "start" then each_node "cd /root/v2/worker && ./start.sh -d"
when "stop" then each_node "cd /root/v2/worker && ./stop.sh -d"
when "update" then each_node "cd /root/v2/worker && git pull && ./stop.sh && ./start/sh -d"
else
    puts "usage:"
    puts "[clear|diskfree|start|stop|update|firstrun]"
end

