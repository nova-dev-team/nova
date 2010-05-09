#!/usr/bin/ruby

require 'yaml'

puts "Phase 2: Installing depended .gem packages..."
puts

# install rubygems
system "
  cp #{File.dirname __FILE__}/../data/src/rubygems-1.3.5.zip /tmp
  cd /tmp
  unzip -q rubygems-1.3.5.zip
  sudo ruby rubygems-1.3.5/setup.rb --no-ri --no-rdoc
  rm -rf rubygems-1.3.5 rubygems-1.3.5.zip
"

# get list of gems provided in the installer
system "
  cd #{File.dirname __FILE__}/../data/gems
  sudo gem install -l *.gem --no-ri --no-rdoc
"

# switch from "gems" folder to "server_side"
puts
puts "Phase 3: Compiling used tools..."
puts
system "
  cd #{File.dirname __FILE__}/../../server_side
  make clean
  make
"

puts
puts "Phase 4: Installing Nova modules..."

conf = YAML::load File.read "#{File.dirname __FILE__}/../../../common/config/conf.yml"
puts "Target dir: #{conf["system_root"]}"

system "
  cd #{File.dirname __FILE__}/../../..
  mkdir -p #{conf["system_root"]}
  cp -r * #{conf["system_root"]}
  cd #{conf["system_root"]}/master
  rake nova:master:init
  cd #{conf["system_root"]}/vm_pool
  rake nova:vm_pool:init
  cd #{conf["system_root"]}/worker
  rake nova:worker:init
"

puts "Everything done!"

