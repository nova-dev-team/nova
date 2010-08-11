#!/usr/bin/ruby

require 'rubygems'
require 'pp'
require 'uuidtools'
require 'fileutils'
require 'json'
require File.dirname(__FILE__) + "/nss_proxy.rb"
require File.dirname(__FILE__) + "/utils.rb"

def write_nss_log message
  unless File.exists? "#{RAILS_ROOT}/log"
    FileUtils.mkdir_p "#{RAILS_ROOT}/log"
  end
  File.open("#{RAILS_ROOT}/log/my_nss_log", "a") do |f|
    message.each_line do |line|
      if line.end_with? "\n"
        f.write "#{Time.now}: #{line}"
      else
        f.write "#{Time.now}: #{line}\n"
      end
    end
  end
end

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
  write_nss_log "Open and write process id file!"
  end
  while 1 do
    fp = File.new(fpath, "w+")

    unless File.exists? fspath
      # create default settings file
      File.open(fspath, "w") do |f|
        f.write "127.0.0.1"
      end
      write_nss_log "'#{fspath}' not exist, create it and write '127.0.0.1'"
    end

    NSS_ADDR = "#{File.open(fspath).readline.chomp}:#{common_conf["nss_port"]}" if File.exist? fspath
    np = NssProxy.new NSS_ADDR
    puts "Created NSS proxy for '#{NSS_ADDR}'"
    puts "NSS proxy status: '#{np.status}'"
    puts "NSS proxy error message: '#{np.error_message}'"
    write_nss_log "Created NSS proxy for '#{NSS_ADDR}', status: '#{np.status}', error message: '#{np.error_message}'"

    config_time = File.mtime(fspath)
    # Record the modify time of file "nss_is_run_updater_script".
    if np.status == "running"
      #puts "OK2"
      if np.role != nil
        fp = File.open(fpath,"w+")
        if fp
          fp.syswrite("role: #{np.role}")
          write_nss_log "Write nss info into file: '#{fpath}'!"
        else
          puts "Unable to open file: '#{fpath}'!"
          write_nss_log "Unable to open file: '#{fpath}'"
        end
      end
    else
      # If not creat NSS proxy, write np.status into the file
      fp = File.open(fpath, "w+")
      fp.syswrite(np.status)
      write_nss_log "NSS proxy is not running, write np.status into file '#{fpath}'"
    end
    # Sleeping, at the meanwhile, check for config file modification every 1 second.
    x = SLEEPTIME
    while x > 0 do
        #puts "OK"
      if config_time != File.mtime(fspath)
        write_nss_log "File :#{fspath} changed, will create new NSS proxy immediately!"
        break;
      end
      x = x - 1
       #puts File.mtime(fpath)
       #puts File.mtime(fspath)
      sleep(1)
    end
  end
end

