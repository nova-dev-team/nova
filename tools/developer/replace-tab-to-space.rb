#!/usr/bin/ruby

# This script replaces indent tabs to space.
# Santa Zhang (santa1987@gmail.com)

# replace leading tab to 2 spaces.
def replace_leading_tab line
  if line.start_with? "\t"
    return "  " + replace_leading_tab(line[1..-1])
  else
    return line
  end
end

if ARGV.length == 0
  puts "This script replaces indent tabs to space."
  puts
  puts "usage: ./replace-tab-to-space.rb <file-path>"
end

f = ARGV[0]
all_lines = File.read f
File.open(f, "w") do |f_out|
  all_lines.each_line do |line|
    f_out.write replace_leading_tab(line)
  end
end

