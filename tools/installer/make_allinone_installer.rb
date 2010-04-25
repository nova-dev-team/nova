#!/usr/bin/ruby

def my_exec cmd
  puts "[cmd] #{cmd}"
  system cmd
end

puts "TODO: require root privilege"

my_exec "#{File.dirname __FILE__}/lib/download_debs.py"

puts "TODO"
