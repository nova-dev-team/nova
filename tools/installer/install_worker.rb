#!/usr/bin/ruby

# This script generates bootstrap worker module installer script, pushes it to worker machine, and installs the machine.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require "#{File.dirname __FILE__}/../../common/lib/utils.rb"

require_root_privilege

puts "This script installs a remote worker machine with the help of ssh & scp."
puts "Make sure the storage server is up and running!"
puts "Usage: install_worker.rb <worker_host>"

exit if ARGV.length == 0

unless File.exists? "#{File.dirname __FILE__}/data/all-in-one/install.sh" and File.exists? "#{File.dirname __FILE__}/data/all-in-one/nova-all-in-one-installer.tar.gz"
  print "!!!"
  print "!!! All-in-one installer package not found! Make sure you have ran './make_allinone_installer.rb' first!"
  print "!!!"
  exit
end

worker_host = ARGV[0]
token = random_token

my_exec <<CMD
  scp -o stricthostkeychecking=no -r #{File.dirname __FILE__}/data/all-in-one #{worker_host}:/tmp/install-nova.#{token}
  ssh -o stricthostkeychecking=no #{worker_host} "cd /tmp/install-nova.#{token} && ./install.sh && rm -rf /tmp/install-nova.#{token}"
CMD

