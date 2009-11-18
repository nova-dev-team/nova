#!/usr/bin/ruby

require 'pp'

def cleanup_ssh_nopass
    puts "cleaning duplicated authorized keys"
    other_lines = []
    ssh_lines = {}
    File.open("/root/.ssh/authorized_keys", "r") do |f|
	f.each_line do |line|
	    if line.start_with? "ssh-rsa "
		if ssh_lines.has_key? line
		    puts "*** duplicated key found:"
		    puts line
		    puts ""
		end
		ssh_lines[line] = 1
	    else
		other_lines << line
	    end
	end
	puts "*** other lines:"
	other_lines.each do |line|
	    puts line
	end
	puts "*** ssh keys"
	ssh_lines.keys.each do |line|
	    puts line
	end
    end
    File.open("/root/.ssh/authorized_keys", "w") do |f|
	ssh_lines.keys.each do |line|
	    f.write line
	end
	other_lines.each do |line|
	    f.write line
	end
    end
end

