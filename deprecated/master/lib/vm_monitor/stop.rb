#!/usr/bin/ruby

# A wrapper around ZJU's vm monitor
# make it easier to kill by pid

require "#{File.dirname(__FILE__)}/../utils.rb"

the_pid_fn = File.dirname(__FILE__) + "/../../tmp/pids/zju_vm_monitor_udpserver.pid"
kill_by_pid_file the_pid_fn

