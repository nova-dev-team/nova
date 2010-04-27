#!/usr/bin/ruby

# This script generates bootstrap worker module installer script, pushes it to worker machine, and installs the machine.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require "#{File.dirname __FILE__}/../../common/lib/utils.rb"

require_root_privilege

puts "This script installs a remote worker machine with the help of ssh & scp."
puts "Make sure the storage server is up and running!"
puts "Usage: install_worker.rb <worker_host> <storage_server>"

exit if ARGV.length == 0

worker_host = ARGV[0]
storage_server = ARGV[1]

token = random_token

# TODO lftp should be already installed on worker machine, or we should copy the lftp .deb package to remote machine

File.open("/tmp/nova-worker-installer.bootstrap.#{token}.sh", "w") do |f|
  f.write <<INSTALLER_CONTENT
#!/bin/bash

# This script is automatically generated by 'install_worker.rb' script of Nova platform.
#
# Author::      Santa Zhang (santa1987@gmail.com)
# Since::       0.3

# create the install folder, most preparation work is done here
mkdir -p nova-worker-installer.#{token}/nova
cd nova-worker-installer.#{token}

# remove possible old download script here
rm -rf download-packages.lftp

# prepare new download script
echo "open #{storage_server}" >> download-packages.lftp
echo "get nova-all-in-one-installer.tar.gz" >> download-packages.lftp

# download the installation package
lftp -f download-packages.lftp

# extract files in the package
cd nova
tar zxf ../nova-all-in-one-installer.tar.gz

# install .deb packages
cd tools/installer/data/debs
dpkg -i --skip-same-version *.deb

# trigger the 'install_stage2.rb' script, since worker & master is the same
cd ../../lib
ruby install_stage2.rb

# all job done, TODO: send back finish message

INSTALLER_CONTENT
end

my_exec "
  cd /tmp
  chmod a+x nova-worker-installer.bootstrap.#{token}.sh
"

