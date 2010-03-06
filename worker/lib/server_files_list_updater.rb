#!/usr/bin/ruby

# This tool is used to update server files list.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require "fileutils"

if ARGV.length == 0
  puts "please provide the 'run_root'"
  puts "usage: server_files_list_updater.rb <run_root>"
  exit 1
end

run_root = ARGV[0]

pid = fork do
  while true
    `lftp -f #{run_root}/lftp_script 2>&1 | tee "#{run_root}/server_files_list"`
    sleep 120 # sleep 2 minutes
  end
end

puts "server_files_list_updater running with pid=#{pid}"
FileUtils.mkdir_p "#{File.dirname __FILE__}/../tmp/pids"
File.open("#{File.dirname __FILE__}/../tmp/pids/server_files_list_updater.pid", "w") do |f|
  f.write pid
end

