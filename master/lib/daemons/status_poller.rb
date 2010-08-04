#!/usr/bin/env ruby

# Run the daemons like this:
#
#    RAILS_ENV=production ./script/daemon start

require 'rubygems'
require 'rest_client'
require 'timeout'
require 'json'
require 'uuidtools'
require 'fileutils'
require 'yaml'

conf = YAML::load File.read "#{File.dirname __FILE__}/../../../common/config/conf.yml"
if conf["master_use_swiftiply"]
  ENV["RAILS_ENV"] ||= "production"
else
  ENV["RAILS_ENV"] ||= "development"
end

require File.dirname(__FILE__) + "/../../config/environment"
require "#{File.dirname __FILE__}/../worker_proxy"

$running = true
Signal.trap("TERM") do
  $running = false
end

# Write some logs into log/my_log
#
# Since::   0.3
def write_log message
  unless File.exists? "#{RAILS_ROOT}/log"
    FileUtils.mkdir_p "#{RAILS_ROOT}/log"
  end
  File.open("#{RAILS_ROOT}/log/my_log", "a") do |f|
    message.each_line do |line|
      if line.end_with? "\n"
        f.write "#{Time.now}: #{line}"
      else
        f.write "#{Time.now}: #{line}\n"
      end
    end
  end
end

# Fetch the body from RestClient reply result.
#
# Since::   0.3
def rep_body rep
  begin
    return rep.body
  rescue
    return rep
  end
end

def loop_body
  #write_log "daemon woke up"
  # connect pending pmachines
  Pmachine.all.each do |pm|
    if pm.status == "pending"
      write_log "contacting pmachine with ip=#{pm.ip}"
      begin
        timeout 10 do
          begin
            write_log "Trying to connect pmachine #{pm.ip}"
            raw_reply = rep_body(RestClient.get "#{pm.root_url}/misc/role.json")
            reply = JSON.parse raw_reply
            if reply["success"] != true or reply["message"] != "worker"
              pm.status = "failure"
              pm.save
              write_log "Failed to connect #{pm.ip}, raw reply is '#{raw_reply}'"
            else
              pm.status = "working"

              # update hostname
              raw_reply = rep_body(RestClient.get "#{pm.root_url}/misc/hostname.json")
              reply = JSON.parse raw_reply
              if reply["success"] == true
                pm.hostname = reply["hostname"]
              end
              pm.save
              write_log "Successfully connected to #{pm.ip}, raw reply is '#{raw_reply}'"
            end
          rescue Exception => e
            pm.status = "failure"
            pm.save
            write_log "Time out connecting #{pm.ip}, raw reply is '#{raw_reply}'. Exception message: #{e.to_s}"
          end
        end
      rescue => e
        pm.status = "failure"
        pm.save
        write_log "Exception: '#{e.to_s}'"
      end
    end
  end

  # connect all working vmachines
  Pmachine.find(:all, :conditions =>'status = "working"').each do |pm|

    # test if worker is still running
    retry_count = 0
    begin
      #write_log "Testing if worker #{pm.ip} is still running"
      wp = pm.worker_proxy
      if wp.status == "failure"
        if retry_count > 3
          write_log "Worker #{pm.ip} is down"
          pm.status = "failure"
          pm.save
        else
          retry_count += 1
          retry
        end
      else
#        write_log "Worker #{pm.ip} still running"
      end
    rescue => e
      write_log "Exception occured when testing liveness of #{pm.ip}: #{e.to_s}"
      unless retry_count > 3
        retry_count += 1
        retry
      end
    end

    if pm.status != "working"
      # the pm status could be changed by the testing code above
      next # go on with next pmachine
    end

    # sync the settings for "storage_server"
    begin
      reply = JSON.parse rep_body(RestClient.get "#{pm.root_url}/settings/show.json?key=storage_server")
      if reply["value"] != Setting.storage_server
        #write_log "sync setting for 'storage_server' to #{pm.ip}"
        RestClient.post "#{pm.root_url}/settings/edit", :key => "storage_server", :value => Setting.storage_server
      end
    rescue => e
      write_log "Exception occured when fetching settings from #{pm.ip}: #{e.to_s}"
    end

    ##############################get workers' log and write these logs to database#############################################

    begin
      #write_log "Fetching perflogs from #{pm.ip}"

      logs = JSON.parse rep_body(RestClient.post "#{pm.root_url}/logs/show.json", :time => (Time.now.to_i - 60))
      logs["data"].each do |log|
        next if PerfLog.find(:first, :conditions => {:pmachine_id => pm.id, :time => log["Time"]}) != nil # prevent duplicate entries
        plog = PerfLog.new
        plog.memFree = log["memFree"]
        plog.pmachine_id = pm.id
        plog.time = log["Time"]
        plog.dSize = log["dSize"]
        plog.dAvail = log["dAvail"]
        plog.Rece = log["Rece"]
        plog.CPU = log["CPU"]
        plog.Tran = log["Tran"]
        plog.memTotal = log["memTotal"]
        plog.save
      end
      time_now = Time.now
      time_str = (time_now - 3600).strftime("%Y%m%d%H%M%S")
      PerfLog.delete_all(["time < ?", time_str])
      #write_log "finished syncing perf logs from #{pm.ip}"
    rescue Exception => e
      write_log "Exception happend when fetching perflogs from #{pm.ip}. Exception: #{e.to_s}"
    end

    ##############################end of get workers' log and write these logs to database#####################################

    # sync info on vm
    begin
      #write_log "Sync VM statuses from #{pm.root_url}/vmachines/index.json"
      raw_reply = rep_body(RestClient.get "#{pm.root_url}/vmachines/index.json")
      reply = JSON.parse raw_reply
      #write_log "Raw reply is: #{raw_reply}"

      # remove VMs that are not running any more
      pm.vmachines.each do |vm|
        vm_found = false
        reply["data"].each do |real_vm|
          if real_vm["uuid"] == vm.uuid
            vm_found = true
            break
          end
        end
        if vm_found == false and vm.status != "start-pending" and vm.status != "boot-failure" and vm.status != "connect-failure" and vm.migrate_to != nil
          write_log "VM '#{vm.name}' is not running any more!"
          vm.log "info", "VM '#{vm.name}' is not running any more!"
          vm.status = "shut-off"
          vm.pmachine.vmachines.delete vm
          vm.pmachine = nil
          vm.save
        end
      end

      # get status of actuall running VMs
      reply["data"].each do |real_vm|
        #write_log "Working on VM with name='#{real_vm["name"]}', uuid=#{real_vm["uuid"]}"

        vm_already_in_db = false
        vm = nil
        pm.vmachines.each do |vm_in_db|
          if vm_in_db.uuid == real_vm["uuid"]
            vm_already_in_db = true
            vm = vm_in_db
            break
          end
        end

        if vm_already_in_db
          #write_log "VM '#{real_vm["name"]}' already in DB"
          if vm.status == "shutdown-pending"
            # destroy VM if it is pending shut-off
            write_log "VM '#{real_vm["name"]}' is to be shut off"
            vm.log "info", "VM '#{real_vm["name"]}' is to be shut off"
            pm.worker_proxy.destroy_vm vm.name
            
            vm.status = "shut-off"
            vm.pmachine.vmachines.delete vm
            vm.pmachine = nil
            vm.save
          else
            if real_vm["vnc_port"] != nil
              vm.vnc_port = real_vm["vnc_port"].to_i
            end
            case real_vm["status"].downcase
            when "preparing"
              vm.status = "start-preparing"
            when "running"
              vm.status = "running"
            when "suspended"
              vm.status = "suspended"
            when "not running"
              if vm.status != "shut-off"
                # check if cleared error message
                vm.status = "boot-failure"
              end
            else
              # ignore
            end
            vm.save
          end
        else
          write_log "VM '#{real_vm["name"]}' not in DB, destroying!"
          begin
            pm.worker_proxy.destroy_vm real_vm["name"]
          rescue => e
            write_log "Exception while destroying ophan '#{real_vm["name"]}': #{e.to_s}"
          end
        end

        # fix vnc port == -1
        fix_vm = Vmachine.find_by_uuid real_vm["uuid"]
        if fix_vm != nil and (fix_vm.vnc_port == nil or fix_vm.vnc_port.to_s == "-1")
          fix_vm.vnc_port = real_vm["vnc_port"]
          fix_vm.save
        end
      end
    rescue
    end
  end

  # OK, now works on all the VM. It is prefered to group VM on same host, so that the cost of worker_proxy
  # will be smaller.
  Vmachine.find(:all, :conditions =>'status = "start-pending"').each do |vm|
    write_log "trying to start vm #{vm.name}"
    vm.log "info", "Trying to start vm #{vm.name}"
    pm = Pmachine.start_vm vm
    if pm == nil
      # not enough machines
      vm.status = "boot-failure"
      vm.save
      write_log "failed to boot #{vm.name}, mark status as 'boot-failure'"
      vm.log "info", "Failed to boot #{vm.name}, mark status as 'boot-failure'"
    else
      vm.status = "start-preparing"
      vm.save
      write_log "triggered #{vm.name}, mark status as 'start-preparing'"
      vm.log "info", "Triggered #{vm.name}, mark status as 'start-preparing'"
    end
  end

  # close VMs.
  Vmachine.find(:all, :conditions =>'status = "shutdown-pending"').each do |vm|
    write_log "Shutting down vm '#{vm.name}'"
    vm.log "info", "Shutting down vm '#{vm.name}'"
    if vm.pmachine != nil
      wp = vm.pmachine.worker_proxy
      wp.destroy_vm vm.name if wp != nil
      vm.status = "shut-off"
      vm.pmachine.vmachines.delete vm
      vm.pmachine.save
      vm.pmachine = nil
      vm.save
    else
      # pmachine is null, which is very unlikely
      write_log "Warning: vm '#{vm.name}' does not have a pmachine!"
      vm.log "warning", "Vmachine '#{vm.name}' does not have a pmachine!"
      vm.status = "shut-off"
      vm.save
    end
  end

  # live migrations
  Vmachine.all.each do |vm|
    begin
      next if vm.pmachine == nil
      next if vm.migrate_to == nil
      vm.log "info", "Migrating from '#{vm.migrate_from}' to '#{vm.migrate_to}'"
      wp = vm.pmachine.worker_proxy
      ret = wp.live_migrate vm.name, vm.migrate_to
      if ret == nil
        # migration failure
        vm.log "error", "Error when migrating from '#{vm.migrate_from}' to '#{vm.migrate_to}'; Worker error message: #{wp.error_message}"
      else
        # migration finished, either success, or failure
        # change the vm's pmachine, if succesfully migrated
        if ret["success"] == true
          dest_pm = Pmachine.find_by_ip vm.migrate_to
          vm.pmachine = dest_pm
          vm.log "info", "Successfully migrated from '#{vm.migrate_from}' to '#{vm.migrate_to}'"
        else
          vm.log "error", "Failed to migrate from '#{vm.migrate_from}' to '#{vm.migrate_to}'"
        end
      end
      vm.migrate_to = nil
      vm.migrate_from = nil
      vm.save
    rescue => e
      write_log "[error] Exception when doing live migration: #{e.to_s}"
      vm.log "exception", "When doing live migration: #{e.to_s}"
    end
  end

  # update vnc_proxy
  Vmachine.all.each do |vm|
    next if vm.pmachine == nil
    pwd = vm.id.to_s  # pwd is vm id
    ip = vm.pmachine.ip.to_s
    port = vm.vnc_port
    next if port == nil
    system "#{RAILS_ROOT}/../tools/server_side/bin/vnc_proxy_ctl del -p #{pwd} -s #{RAILS_ROOT}/tmp/sockets/vnc_proxy.sock"
    system "#{RAILS_ROOT}/../tools/server_side/bin/vnc_proxy_ctl add -p #{pwd} -d #{ip}:#{port} -s #{RAILS_ROOT}/tmp/sockets/vnc_proxy.sock"
  end

  sleep 1
end


while($running) do
  begin
    loop_body
  rescue => e
    write_log "Exception: #{e.to_s}"
    e.backtrace.each_line do |line|
      write_log "Backtrace: #{line}"
    end
  end
end
