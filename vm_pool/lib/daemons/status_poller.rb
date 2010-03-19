#!/usr/bin/env ruby

# Run the daemons like this:
#
#    RAILS_ENV=production ./script/daemon start

require 'rubygems'
require 'rest_client'
require 'timeout'
require 'json'
require 'yaml'

ENV["RAILS_ENV"] ||= "production"

require File.dirname(__FILE__) + "/../../config/environment"

$running = true
Signal.trap("TERM") do 
  $running = false
end

while($running) do

  conf = YAML::load File.read "#{File.dirname __FILE__}/../../../common/config/conf.yml"

  # connect pending pmachines  
  Pmachine.all.each do |pm|
    if pm.status == "pending"
      begin
        timeout 5 do
          begin
            ActiveRecord::Base.logger.info "#{Time.now}: Trying to connect pmachine #{pm.ip}:#{conf["worker_port"]}\n"
            raw_reply = RestClient.get "http://#{pm.ip}:#{conf["worker_port"]}/misc/role.json"
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
  
  sleep 10
end
