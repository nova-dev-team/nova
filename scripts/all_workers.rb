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
when "rmcache" then each_node "rm -r /root/v2/worker/tmp/work_site/storage_cache"
when "rmlog" then each_node "rm -r /root/v2/worker/log"
when "firstrun" then each_node "cd /root/v2/worker && ./first_run.sh -d"
when "start" then each_node "cd /root/v2/worker && ./start.sh -d"
when "stop" then each_node "cd /root/v2/worker && ./stop.sh -d"
when "update" then each_node "cd /root/v2/worker && git pull"
else
    if ARGV[0] == nil
        puts "usage:"
	puts "[clear|diskfree|start|stop|update|firstrun]"
    else
	each_node(ARGV.join " ")
    end
end

