#!/usr/bin/ruby

# A wrapper around ZJU's vm monitor
# make it easier to kill by pid

require "#{File.dirname(__FILE__)}/../utils.rb"

the_pid_fn = File.dirname(__FILE__) + "/../../tmp/pids/zju_vm_monitor_udpserver.pid"
kill_by_pid_file the_pid_fn


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

