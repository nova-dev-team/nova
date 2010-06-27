#!/usr/bin/ruby

# This is the style checker for Nova project.
# Santa Zhang (santa1987@gmail.com)

require 'find'

load File.join File.dirname(__FILE__), 'style-check.conf'

NOVA_SRC_ROOT = "../.."

Find.find(NOVA_SRC_ROOT) do |f|
  if f.end_with? ".c"
    puts "[C] #{f}"
  elsif f.end_with? ".cpp"
    puts "[C++] #{f}"
  elsif f.end_with? ".h"
    puts "[C/C++ Header] #{f}"
  elsif f.end_with? ".rb"
    puts "[Ruby] #{f}"
  elsif f.end_with? ".py"
    puts "[Python] #{f}"
  end
end

puts "TODO"

