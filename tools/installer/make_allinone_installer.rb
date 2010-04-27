#!/usr/bin/ruby

# This script creates an all-in-one installer for workers, and pushes it onto storage server (FTP).
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require "#{File.dirname __FILE__}/../../common/lib/utils.rb"

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

require_root_privilege
my_exec "#{File.dirname __FILE__}/lib/download_debs.py"
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
  tar cvzf ../nova-all-in-one-installer.#{token}.tar.gz *
"

puts
puts "########################  INSTALLER CREATED  #######################"
puts
puts "Now please provide the FTP addresss. Include the username, password and port info in URL, like 'ftp://santa:santa@localhost:8021/'"
ftp_url = gets.chomp

File.open("/tmp/nova-all-in-one-installer.#{token}.lftp", "w") do |f|
  f.write <<LFTP
open #{ftp_url}
put nova-all-in-one-installer.#{token}.tar.gz -o nova-all-in-one-installer.tar.gz
LFTP
end

my_exec "
  cd /tmp
  lftp -f nova-all-in-one-installer.#{token}.lftp
  rm -rf nova-all-in-one-installer.#{token}*
"

puts "Everything done!"

