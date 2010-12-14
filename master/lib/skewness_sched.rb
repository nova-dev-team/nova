#!/usr/bin/ruby

# A wrapper around the PKU skewness scheduler
# make it easier to use.

require "fileutils"
load "#{File.dirname __FILE__}/skewness_sched.params.rb"

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

def skewness_real_run layout, pm_ip_map
  # TODO collect data
  # do migrationg

  FileUtils.cp_r "#{File.dirname __FILE__}/skewness_sched", "#{File.dirname __FILE__}/../tmp/skewness/skewness_sched"
  FileUtils.cp "#{File.dirname __FILE__}/vm_monitor/udpclient.py", "#{File.dirname __FILE__}/../tmp/skewness/skewness_sched"

  # generate config file
  sched_run_dir = "#{File.dirname __FILE__}/../tmp/skewness/skewness_sched"

  visited_pmachines = []
  File.open("#{sched_run_dir}/config/capacity.py", "w") do |f|
    f.write "capacity = {\n"
    layout.values.sort.uniq.each do |pm|
      cap = nil
      $pm_capacity.keys.each do |key|
        if key.class == [].class
          if key.include? pm
            cap = $pm_capacity[key]
            break
          end
        else
          if key == pm
            cap = $pm_capacity[key]
            break
          end
        end
      end
      if cap == nil
        cap = $pm_capacity[".default"]
      end
      f.write "  '#{pm}': {"
      cap.each do |k, v|
        f.write "'#{k}':#{v}, "
      end
      f.write "},\n"
    end
    f.write "\n}\n\n"
  end

  File.open("#{sched_run_dir}/config/config.py", "w") do |f|
    f.write "layout = {\n"
    f.write((layout.collect {|k, v| "  '#{k}': '#{v}'"}).join ",\n")
    f.write "\n}\n\n"
    f.write File.read("#{sched_run_dir}/config/capacity.py")
    f.write "\n"
    f.write $script_body
  end

  File.open("#{sched_run_dir}/config/load.py", "w") do |f|
    f.write "history=[\n"
    f.write "[1, {\n"
    # TODO writing dummy data


    layout.keys.each do |vm|
      f.write "   '#{vm}':{\n"
      f.write "     'cpu_ns_1':{0:5.5E8,},\n"
      f.write "     'ram_MB':1053,\n"
      f.write "     'tx_bytes_1':{0:34981350,},\n"
      f.write "     'rx_bytes_1':{0:35363130,},\n"
      f.write "     'rd_req_1':{0:0.0},\n"
      f.write "     'wr_req_1':{0:0.0},\n"
      f.write "    },\n"
    end

    f.write "}]\n"
    f.write "]\n"
  end

end

# return "already running"
# or "new round"
def skewness_run layout, pm_ip_map
  # check if there is already an instance running

  pid_fn = "#{File.dirname __FILE__}/../tmp/pids/skewness_runner.pid"

  is_running = false
  if File.exists? pid_fn
    # check if really running
    pid = File.read(pid_fn).to_i
    begin
      Process.kill 0, pid
      is_running = true
    rescue
      is_running = false
    end
  end

  if is_running
    # still running
    return "already running"
  else
    fork do
      # double forking
      exit if fork

      File.open(pid_fn, "w") do |f|
        f.write Process.pid
      end

      skewness_real_run layout, pm_ip_map
      FileUtils.rm pid_fn
    end
    return "new round"
  end

end



if $0 == __FILE__
  puts "This is for testing only!"
  if skewness_has_result == false
    puts "No plan available:"
    vm_layout = {"vm1" => "femi", "vm2" => "pm2", "vm3" => "pm2", "vm4" => "pm2"}
    pm_ip_map = {"femi" => "10.0.1.241", "pm2" => "10.0.1.241"}
    puts (skewness_run vm_layout, pm_ip_map)
  else
    plan = skewness_consume_result
    puts "Got a plan:"
    puts plan
  end
end

