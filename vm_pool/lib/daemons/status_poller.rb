#!/usr/bin/env ruby

# Run the daemons like this:
#
#    RAILS_ENV=production ./script/daemon start

require 'rubygems'
require 'rest_client'
require 'timeout'
require 'json'
require 'uuidtools'

ENV["RAILS_ENV"] ||= "production"

require File.dirname(__FILE__) + "/../../config/environment"

$running = true
Signal.trap("TERM") do 
  $running = false
end

while($running) do
  # connect pending pmachines  
  Pmachine.all.each do |pm|
    if pm.status == "pending"
      begin
        timeout 5 do
          begin
            ActiveRecord::Base.logger.info "#{Time.now}: Trying to connect pmachine #{pm.ip}\n"
            raw_reply = RestClient.get "#{pm.root_url}/misc/role.json"
            reply = JSON.parse raw_reply
            if reply["success"] != true or reply["message"] != "worker"
              pm.status = "failure"
              pm.save
              ActiveRecord::Base.logger.error "#{Time.now}: failure! raw_reply is #{raw_reply}\n"
            else
              pm.status = "working"
              pm.save
            end
          rescue
            pm.status = "failure"
            pm.save
            ActiveRecord::Base.logger.error "#{Time.now}: failure! time out!\n"
          end
        end
      rescue => e
        pm.status = "failure"
        pm.save
        ActiveRecord::Base.logger.error "#{Time.now}: exception #{e.to_s}!\n"
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
      reply = JSON.parse RestClient.get "#{pm.root_url}/vmachines.json"
      reply.data do |real_vm|
        if pm.vmachines.fine_by_uuid real_vm.uuid != nil
          vm.status = real_vm["status"]
          if real_vm["vnc_port"] != nil
            vm.vnc_port = real_vm["vnc_port"].to_i
          end
        else
          vm = Vmachine.new
          vm.name = real_vm["name"]
          vm.uuid = real_vm["uuid"]
          vm.status = real_vm["status"].downcase
          if real_vm["vnc_port"] != nil
            vm.vnc_port = real_vm["vnc_port"].to_i
          end
          pm.vmachines << vm
          vm.save
        end
      end
    rescue
    end

    if pm.vmachines.size < pm.pool_size
      # create new vm
      vm = Vmachine.new
      vm.save # for vm.id
      vm.name = "vmp-#{pm.id}-#{vm.id}"
      vm.uuid = UUIDTools::UUID.random_create.to_s
      vm.status = "preparing"
      pm.vmachines << vm
      vm.save

      reply = JSON.parse RestClient.post "#{pm.root_url}/vmachines/start.json",
        :arch => Setting.find_by_key("vm_arch").to_i,
        :cpu_count => Setting.find_by_key("vm_cpu_count").to_i,
        :hda_image => Setting.find_by_key("vm_hda_image"),
        :hypervisor => Setting.find_by_key("vm_hypervisor"),
        :mem_size => Setting.find_by_key("vm_mem_size").to_i,
        :run_agent => false,
        :uuid => vm.uuid,
        :name => vm.name

      unless replay["success"]
        vm.status = "failure"
        vm.save
      end

    elsif pm.vmachines.size > pm.pool_size
      # stop unused vm
    end
  end
  
  sleep 10
end

