#!/usr/bin/ruby

require 'rubygems'
require 'pp'
require 'uuidtools'
require 'fileutils'
require 'json'
require File.dirname(__FILE__) + "/nss_proxy.rb"
require File.dirname(__FILE__) + "/utils.rb"

# Sleep time, the unit is second.
SLEEPTIME = 120
# Because of NFS, nss use 127.0.0.1 for default.
NSS_ADDR = "127.0.0.1:#{common_conf["nss_port"]}"

fspath = File.join common_conf["run_root"], "nss_is_run_updater_script"
fpath = File.join common_conf["run_root"], "nss_is_run"

fork do   
  # write a pid file
  File.open(File.dirname(__FILE__) + "/../tmp/pids/nss_is_run.pid", "w") do |f|
    f.write Process.pid
  end
  while 1 do
	  fp = File.new(fpath, "w+")
	  NSS_ADDR = "#{File.open(fspath).readline.chomp}:#{common_conf["nss_port"]}" if File.exist? fspath
    np = NssProxy.new NSS_ADDR
    puts "Created NSS proxy for '#{NSS_ADDR}'"
    puts "NSS proxy status: '#{np.status}'"
    puts "NSS proxy error message: '#{np.error_message}'"
    config_time = File.mtime(fspath)
    # Record the modify time of file "nss_is_run_updater_script".
    if np.status == "running"
      #puts "OK2"
      if np.hostname != nil
        fp = File.open(fpath,"w+")
        if fp
          fp.syswrite("hostname: #{np.hostname}")
        else
          puts "Unable to open file: '#{fpath}'!" 
        end
      end
    else
      # If not creat NSS proxy, write np.status into the file
      fp = File.open(fpath, "w+")
      fp.syswrite(np.status)
    end
    # Sleeping, at the meanwhile, check for config file modification every 1 second. 
    x = SLEEPTIME
    while x > 0 do
        #puts "OK"
      if config_time != File.mtime(fspath)
        break;
      end
      x = x - 1
       #puts File.mtime(fpath)
       #puts File.mtime(fspath)
      sleep(1)
    end
  end
end
