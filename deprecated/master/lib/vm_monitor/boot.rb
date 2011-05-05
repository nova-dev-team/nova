#!/usr/bin/ruby

# A wrapper around ZJU's vm monitor
# make it easier to kill by pid

require "#{File.dirname(__FILE__)}/../utils.rb"

the_pid_fn = File.dirname(__FILE__) + "/../../tmp/pids/zju_vm_monitor_udpserver.pid"
if kill_by_pid_file the_pid_fn
  # wait a while to start the server, otherwise you might get a socket error
  grace_time = 10
  puts "sleep #{grace_time} seconds before restarting the server"
  sleep grace_time
end

if common_conf["enable_skewness_sched"] != true
  puts "skewness scheduler not enabled, zju vm monitor not necessary"
end


fork do
  # double forking
  exit if fork

  # daemonize
  Process.setsid
  File.open(the_pid_fn, "w") do |f|
    f.write Process.pid
    puts "Starting zju vm monitor"
  end
  Dir.chdir File.expand_path(File.dirname __FILE__)
  File.umask 0000
  STDIN.reopen "/dev/null"
  STDOUT.reopen "/dev/null", "a"
  STDOUT.reopen STDOUT
  exec "python #{File.dirname __FILE__}/udpserver.py"
end

