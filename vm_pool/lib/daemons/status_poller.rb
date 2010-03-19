#!/usr/bin/env ruby

# Run the daemons like this:
#
#    RAILS_ENV=production ./script/daemon start

require 'rubygems'
require 'rest_client'
require 'timeout'
require 'json'

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
    begin
      # sync the settings for "storage_server"
      reply = JSON.parse RestClient.get "#{pm.root_url}/settings/show.json?key=storage_server"
      if reply["value"] != Setting.storage_server
        RestClient.post "#{pm.root_url}/settings/edit", :key => "storage_server", :value => Setting.storage_server
      end
    rescue
    end

    if pm.vmachines.size < pm.pool_size
      # create new vm
    elsif pm.vmachines.size > pm.pool_size
      # stop unused vm
    end

    # TODO poll vmachine status

  end
  
  sleep 10
end

