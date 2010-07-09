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
  # connect pending pmachines
  Pmachine.all.each do |pm|
    if pm.status == "pending"
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
        unless vm_found
          write_log "VM '#{vm.name}' is not running any more!"
          Vmachine.delete vm
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
          if vm.status == "garbaged"
            # destroy VM if it is used too many times
            write_log "VM '#{real_vm["name"]}' is used too many times, garbage collecting..."
            RestClient.post "#{pm.root_url}/vmachines/destroy.json", :uuid => vm.uuid
          else
            vm.status = real_vm["status"]
            if real_vm["vnc_port"] != nil
              vm.vnc_port = real_vm["vnc_port"].to_i
            end
            vm.save
          end
        else
          write_log "VM '#{real_vm["name"]}' not in DB"
          vm = Vmachine.new
          vm.name = real_vm["name"]
          vm.uuid = real_vm["uuid"]
          vm.status = real_vm["status"]
          if real_vm["vnc_port"] != nil
            vm.vnc_port = real_vm["vnc_port"].to_i
          end
          pm.vmachines << vm
          vm.save
        end
      end
    rescue
    end

    if pm.vmachines.size < pm.vm_pool_size
      write_log "Increase VM pool size on Pmachine #{pm.ip}"
      # create new vm
      vm = Vmachine.new
      vm.name = "vmp-#{pm.id}-new"
      vm.uuid = UUIDTools::UUID.random_create.to_s
      vm.save # for vm.id
      vm.name = "vmp-#{pm.id}-#{vm.id}"
      vm.status = "preparing"
      pm.vmachines << vm
      vm.save

      raw_reply = RestClient.post "#{pm.root_url}/vmachines/start.json",
        :arch => Setting.find_by_key("vm_arch").value,
        :cpu_count => Setting.find_by_key("vm_cpu_count").value.to_i,
        :hda_image => Setting.find_by_key("vm_hda_image").value,
        :hypervisor => Setting.find_by_key("vm_hypervisor").value,
        :mem_size => Setting.find_by_key("vm_mem_size").value.to_i,
        :run_agent => false,
        :uuid => vm.uuid,
        :name => vm.name

      reply = JSON.parse raw_reply
      write_log "Reply from worker: #{reply}"
      unless reply["success"]
        vm.status = "failure"
        vm.save
      end

    elsif pm.vmachines.size > pm.vm_pool_size
      # TODO stop unused vm
    end
  end

  # update vnc_proxy
  Vmachine.all.each do |vm|
    pwd = vm.name[4..-1]
    ip = vm.pmachine.ip
    port = vm.vnc_port
    next if port == nil
    system "#{RAILS_ROOT}/../tools/server_side/bin/vnc_proxy_ctl add -p #{pwd} -d #{ip}:#{port} -s #{RAILS_ROOT}/tmp/sockets/vnc_proxy.sock"
  end

  sleep 10
end

