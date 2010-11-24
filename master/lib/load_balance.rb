#!/usr/bin/ruby

# Automatical load balance for virtual machines.
#
# Author::  Santa Zhang (santa1987@gmail.com)
# Since::   0.3.5

require 'rubygems'
require 'fileutils'
require "#{File.dirname __FILE__}/utils"
require 'xmlsimple'

LOGFILE = "#{File.dirname __FILE__}/../log/load_balance.log"
def write_log message
  puts message
  File.open(LOGFILE, "a") do |f|
    message.each_line do |line|
      if line.end_with? "\n"
        f.write "[#{Time.now}] #{line}"
      else
        f.write "[#{Time.now}] #{line}\n"
      end
    end
  end
end

def run_as_daemon pid_fn, &b
  fork do
    # exist if is parent process
    exit if fork
    Process.setsid
    # write pid file
    File.open(pid_fn, "w") do |f|
      f.write Process.pid
    end
    Dir.chdir File.expand_path(File.dirname(__FILE__))
    File.umask 0000
    STDIN.reopen "/dev/null"
    STDOUT.reopen "/dev/null", "a"
    STDERR.reopen STDOUT

    yield b
  end
end

def run_load_balance
  last_time_was_off = false # a variable used to remember last time's status
  last_time_was_on = false
  while true
    sleep 10 # check again every 10 seconds
    should_do_load_balance = false
    if File.exists? "#{File.dirname __FILE__}/../log/load_balance.on"
      should_do_load_balance = true
    end
    if should_do_load_balance == false
      if last_time_was_off == false
        write_log "Load balance is OFF"
        last_time_was_off = true
      end
      last_time_was_on = false
      next
    end
    
    if last_time_was_on == false
      write_log "Load balance is ON"
      last_time_was_on = true
    end


    # do real work, load balancing
    # migrate one vm per round
    write_log "TODO: load balancer"
    last_time_was_off = false
  end
end

run_as_daemon("#{File.dirname __FILE__}/../log/load_balance.pid") do
  run_load_balance
end

