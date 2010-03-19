#!/usr/bin/ruby

puts "Phase 2: Installing depended .gem packages..."
puts

# install rubygems
system "
  cp #{File.dirname __FILE__}/data/src/rubygems-1.3.5.zip /tmp
  cd /tmp
  unzip -q rubygems-1.3.5.zip
  sudo ruby rubygems-1.3.5/setup.rb --no-ri --no-rdoc
  rm -rf rubygems-1.3.5 rubygems-1.3.5.zip
"

# get list of gems provided in the installer
Dir.chdir "#{File.dirname __FILE__}/data/gems"
system "sudo gem install -l *.gem --no-ri --no-rdoc"

# switch from "gems" folder to "server_side"
Dir.chdir "../../../server_side"
puts
puts "Phase 3: Compiling used tools..."
puts
system "
  make clean
  make
"

