#!/usr/bin/ruby

require 'nodelist.rb' # for sys_exec function

def empty_line? s
  s =~ /^[ \t]*$/
end


def time_str
  n = Time.now
  "#{n.year}-#{n.month}-#{n.day}_#{n.hour}-#{n.min}-#{n.sec}"
end

def timed_backup file_path
  bkup_name ="#{file_path}.orig.#{time_str}"
  `cp #{file_path} #{bkup_name}`
  bkup_name
end


def leading_tab_to_space str
  str2 = ""
  is_leading = true
  str.each_char do |c|
    if is_leading
      if c == '\t'
        str2 += "  "
      elsif c != ' '
        is_leading = false
        str2 += c
      else
        str2 += c
      end
    else
      str2 += c
    end
  end
  str2
end

def nothing_changed? file, backup_file
  no_change = false
  IO.popen("diff #{file} #{backup_file}") do |f|
    no_change = true if f.readlines.length == 0
  end
  return no_change
end

def format_file file, backup_file
  File.open(file, "w") do |f|
    File.open(backup_file, "r") do |bf|
      bf.each_line do |line|
        line = leading_tab_to_space line
        if empty_line? line
          f.write "\n"
        else
          f.write line
        end
      end
    end
  end
  sys_exec "diff #{file} #{backup_file}"

  if nothing_changed? file, backup_file
    `mv #{backup_file} #{file}`
    puts "Nothing was changed, roll back action"
  end
end


## main work is done here

if ARGV.length == 0
  puts "usage: format_code.rb <file_to_be_formatted>"
  exit
end

backup_filename = timed_backup ARGV[0]
puts "Original file backup: #{backup_filename}"

format_file ARGV[0], backup_filename
