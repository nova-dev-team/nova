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
require "#{File.dirname __FILE__}/../worker_proxy"

conf = YAML::load File.read "#{File.dirname __FILE__}/../../../common/config/conf.yml"
if conf["master_use_swiftiply"]
  ENV["RAILS_ENV"] ||= "production"
else
  ENV["RAILS_ENV"] ||= "development"
end

require File.dirname(__FILE__) + "/../../config/environment"

$running = true
Signal.trap("TERM") do
  $running = false
end

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

while($running) do
  write_log "daemon woke up"
  # connect pending pmachines
  Pmachine.all.each do |pm|
    if pm.status == "pending"
      write_log "contacting pmachine with ip=#{pm.ip}"
      begin
        timeout 10 do
          begin
            write_log "Trying to connect pmachine #{pm.ip}"
            raw_reply = RestClient.get "#{pm.root_url}/misc/role.json"
            reply = JSON.parse raw_reply
            if reply["success"] != true or reply["message"] != "worker"
              pm.status = "failure"
              pm.save
              write_log "Failed to connect #{pm.ip}, raw reply is '#{raw_reply}'"
            else
              pm.status = "working"

              # update hostname
              raw_reply = RestClient.get "#{pm.root_url}/misc/hostname.json"
              reply = JSON.parse raw_reply
              if reply["success"] == true
                pm.hostname = reply["hostname"]
              end
              ##############################get workers' log and write these logs to database#############################################
              
              logs = RestClient.get "#{pm.root_url}/logs/show.json"
              logs[data].each do |log|
                log["pmachine_id"] = pm.id
                PerfLog.create(log)
              end
              time_now = Time.now
              time_str = (time_now - 3600).strftime("%Y%m%d%H%M%S")
              PerfLog.delete_all(["time < ?", time_str)
              
              ##############################end of get workers' log and write these logs to database#####################################
              pm.save
            end
          rescue
            pm.status = "failure"
            pm.save
            write_log "Time out connecting #{pm.ip}, raw reply is '#{raw_reply}'"
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

    # sync the settings for "storage_server"
    begin
      reply = JSON.parse RestClient.get "#{pm.root_url}/settings/show.json?key=storage_server"
      if reply["value"] != Setting.storage_server
        RestClient.post "#{pm.root_url}/settings/edit", :key => "storage_server", :value => Setting.storage_server
      end
    rescue
    end

    # sync info on vm
    begin
      write_log "Sync VM statuses from #{pm.root_url}/vmachines/index.json"
      raw_reply = RestClient.get "#{pm.root_url}/vmachines/index.json"
      reply = JSON.parse raw_reply
      write_log "Raw reply is: #{raw_reply}"

      # remove VMs that are not runing any more
      pm.vmachines.each do |vm|
        vm_found = false
        reply["data"].each do |real_vm|
          if real_vm["uuid"] == vm.uuid
            vm_found = true
            break
          end
        end
        if vm_found == false and vm.status != "start-pending" and vm.status != "boot-failure" and vm.status != "connect-failure"
          write_log "VM '#{vm.name}' is not running any more!"
          vm.status = "shut-off"
          vm.pmachine.vmachines.delete vm
          vm.pmachine = nil
          vm.save
        end
      end

      # get status of actuall running VMs
      reply["data"].each do |real_vm|
        write_log "Working on VM with name='#{real_vm["name"]}', uuid=#{real_vm["uuid"]}"

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
          write_log "VM '#{real_vm["name"]}' already in DB"
          if vm.status == "shutdown-pending"
            # destroy VM if it is pending shut-off
            write_log "VM '#{real_vm["name"]}' is to be shut off"
            RestClient.post "#{pm.root_url}/vmachines/destroy.json", :uuid => vm.uuid
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
          write_log "VM '#{real_vm["name"]}' not in DB, ignored!"
        end
      end
    rescue
    end
  end

  # OK, now works on all the VM. It is prefered to group VM on same host, so that the cost of worker_proxy
  # will be smaller.
  Vmachine.find(:all, :conditions =>'status = "start-pending"').each do |vm|
    write_log "trying to start vm #{vm.name}"
    pm = Pmachine.start_vm vm
    if pm == nil
      # not enough machines
      vm.status = "boot-failure"
      vm.save
      write_log "failed to boot #{vm.name}, mark status as 'boot-failure'"
      # TODO set up vm info here
    else
      vm.status = "start-preparing"
      vm.save
      write_log "triggered #{vm.name}, mark status as 'start-preparing'"
    end
  end

  # close VMs.
  Vmachine.find(:all, :conditions =>'status = "shutdown-pending"').each do |vm|
    wp = vm.pmachine.worker_proxy
    wp.destroy_vm vm.uuid
    vm.status = "shut-off"
    vm.pmachine.vmachines.delete vm
    vm.pmachine = nil
    vm.save
  end

  sleep 1
end

