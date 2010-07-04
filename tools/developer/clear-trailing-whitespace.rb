#!/usr/bin/ruby

# This script removes useless trailing spaces.
# Santa Zhang (santa1987@gmail.com)

# drop trailing white space
def trim_right line
  line = line.chomp # trim newline char
  while line.end_with? ' ' or line.end_with? '\t'
    line = line[0...line.length]
  end
  line
end

if ARGV.length == 0
  puts "This script removes useless trailing spaces."
  puts ""
  puts "usage: ./clear-trailing-whitespace.rb <file-path>"
  exit 0
end

f = ARGV[0]

all_lines = File.read f
File.open(f, "w") do |f_out|
  all_lines.each_line do |line|
    f_out.write line
  end
end

