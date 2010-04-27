#!/usr/bin/ruby

# This script creates an all-in-one installer for workers, and pushes it onto storage server (FTP).
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require "#{File.dirname __FILE__}/../../common/lib/utils.rb"

require_root_privilege

if File.exists? "#{File.dirname __FILE__}/data/debs"
  empty_deb_folder = true
  Dir.foreach("#{File.dirname __FILE__}/data/debs") do |entry|
    if entry.end_with? ".deb"
      empty_deb_folder = false
      break
    end
  end
  unless empty_deb_folder
    puts "!!!"
    puts "!!! We found some .deb packages already downloaded in data/debs dir."
    puts "!!! They might be stale, we suggest you press Ctrl-C to stop the script now, and remove data/debs dir manually first."
    puts "!!! Or you can press ENTER and continue the script."
    puts "!!!"
    gets # pause
  end
end

if File.exists? "#{File.dirname __FILE__}/data/all-in-one/install.sh" or File.exists? "#{File.dirname __FILE__}/data/all-in-one/nova-all-in-one-installer.tar.gz"
  puts "!!!"
  puts "!!! We found that an all-in-one installer already exists under 'data/all-in-one'!"
  puts "!!! It will be overwritten by newly created installer package."
  puts "!!! You can press Ctrl-C to stop the script now, or press ENTER to continue."
  puts "!!!"
end

#my_exec "#{File.dirname __FILE__}/lib/download_debs.py"
token = random_token
tmp_dir = "/tmp/nova-all-in-one-installer.#{token}"
my_exec "mkdir -p #{tmp_dir}"

cruft_list = []
File.read("#{File.dirname __FILE__}/data/installer_cruft_list").each_line do |line|
  line = line.strip
  next if line.start_with? "#" or line == ""
  cruft_list << line
end

my_exec "
  cd ../..
  cp -r -v * #{tmp_dir}
  cd #{tmp_dir}
  #{(cruft_list.collect {|item| "rm -rf #{item}"}).join "\n"}
  tar cvzf /tmp/nova-all-in-one-installer.#{token}.tar.gz *
"

my_exec "
  mkdir -p #{File.dirname __FILE__}/data/all-in-one
  mv /tmp/nova-all-in-one-installer.#{token}.tar.gz #{File.dirname __FILE__}/data/all-in-one/nova-all-in-one-installer.tar.gz
  rm -rf /tmp/nova-all-in-one-installer.#{token}*
"

File.open("#{File.dirname __FILE__}/data/all-in-one/install.sh", "w") do |f|
  f.write <<INSTALL_SH
#!/bin/bash
tar zxf nova-all-in-one-installer.tar.gz
cd tools/installer/data/debs
dpkg -i *.deb
cd ../../lib
ruby install_stage2.rb
INSTALL_SH
end

my_exec "
  chmod a+x #{File.dirname __FILE__}/data/all-in-one/install.sh
"

puts "Everything done!"

