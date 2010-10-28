#!/usr/bin/ruby

# This script deploys a newly compiled FlexUI project into master model's public folder.
#
# Author::    Santa Zhang (santa1987@gmail.com)

def my_exec cmd
  puts "[cmd] #{cmd}"
  system cmd
end

my_exec "rm -Rf #{File.dirname __FILE__}/../master/public/flexui"
if File.exists? "#{File.dirname __FILE__}/bin-release"
  my_exec "cp -R #{File.dirname __FILE__}/bin-release #{File.dirname __FILE__}/../master/public/flexui"
  puts "*** RELEASE binary copied to master module"
else
  my_exec "cp -R #{File.dirname __FILE__}/bin-debug #{File.dirname __FILE__}/../master/public/flexui"
  puts "*** DEBUG binary copied to master module"
end
