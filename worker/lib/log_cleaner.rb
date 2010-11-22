#!/usr/bin/ruby

require 'rubygems'
require 'libvirt'
require 'fileutils'
require "#{File.dirname __FILE__}/utils"
require 'xmlsimple'

LOGFILE = "#{File.dirname __FILE__}/../log/log_cleaner.log"
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

fork do
  # exist if is parent process
  exit if fork
  Process.setsid
  # write pid file
  File.open("#{File.dirname __FILE__}/../log/log_cleaner.pid", "w") do |f|
    f.write Process.pid
  end
  Dir.chdir File.expand_path(File.dirname(__FILE__))
  File.umask 0000
  STDIN.reopen "/dev/null"
  STDOUT.reopen "/dev/null", "a"
  STDERR.reopen STDOUT

  log_max_size = 1024 * 1024 * 16
  while true
    log_folder = "#{File.dirname __FILE__}/../log"
    Dir.foreach(log_folder) do |entry|
      next if entry.start_with? "."
      next unless (entry.end_with? ".log" or entry.end_with? "_log")
      fullpath = File.join log_folder, entry
      puts "checking log file: #{fullpath}"
      if (File.size fullpath) > log_max_size
        File.truncate fullpath, 0
        write_log "truncate log file: #{fullpath}"
      end
    end
    sleep 60 # scan every 60 sec
  end

end

