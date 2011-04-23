# This is the Rakefile for managing my Workspace
#
# Santa Zhang (santa1987@gmail.com)

require 'fileutils'

def my_exec cmds
  cmds.each_line do |cmd|
    cmd = cmd.strip
    puts "[cmd] #{cmd}"
    system cmd
  end
end

desc "Make distribution tar package"
task :dist do
  puts "TODO make distribution tar"
end

desc "Check coding style"
task :check do
  puts "TODO check coding style"
end

desc "Fix coding style"
task :fix do
  puts "TODO fix codeing style"
end

desc "Announce new version"
task :announce do
  puts "TODO announce new version"
end

task :default => :check

