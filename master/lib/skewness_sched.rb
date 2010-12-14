#!/usr/bin/ruby

# A wrapper around the PKU skewness scheduler
# make it easier to use.

require "fileutils"

def skewness_has_result
  plans_folder = "#{File.dirname __FILE__}/../tmp/skewness/plans"
  FileUtils.mkdir_p plans_folder
  Dir.foreach(plans_folder) do |entry|
    if entry.end_with? ".plan"
      puts entry
      return true
    end
  end
  false
end

# get a sched result, and remove it from saved results
# return: (vm_name, new_pm) or nil
def skewness_consume_result
  nil
end

def skewness_run layout, load_info
  # check if there is already an instance running

end



if $0 == __FILE__
  puts "This is for testing only!"
  if skewness_has_result == false
    puts "No plan available:"
    vm_layout = {"vm1" => "pm1", "vm2" => "pm2"}
    load_info = {}
    skewness_run vm_layout, load_info
  else
    plan = skewness_consume_result
    puts "Got a plan:"
    puts plan
  end
end

