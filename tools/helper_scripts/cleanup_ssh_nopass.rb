#!/usr/bin/ruby

require "fileutils"

def cleanup_ssh_nopass home_folder
  return unless File.exists? "#{home_folder}/.ssh/authorized_keys"
  other_lines = []
  ssh_lines = {}
  FileUtils.cp "#{home_folder}/.ssh/authorized_keys", "#{home_folder}/.ssh/authorized_keys~" # do backup
  File.open("#{home_folder}/.ssh/authorized_keys", "r") do |f|
    f.each_line do |line|
      if line.start_with? "ssh-rsa "
        ssh_lines[line] = 1
      else
        other_lines << line
      end
    end
  end
  File.open("#{home_folder}/.ssh/authorized_keys", "w") do |f|
    ssh_lines.keys.each do |line|
      f.write line
    end
    other_lines.each do |line|
      f.write line
    end
  end
end

# work like C-main()
if __FILE__ == $0
  puts "cleaning duplicated authorized keys"
  cleanup_ssh_nopass ENV["HOME"]
  puts "done!"
end

