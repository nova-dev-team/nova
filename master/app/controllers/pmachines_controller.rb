require 'rubygems'
require 'rest_client'

class PmachinesController < ApplicationController

  before_filter :require_admin_privilege

  def index
    result = []
    Pmachine.all.each do |pmachine|
      result << "#{pmachine.ip}:#{pmachine.port}"
    end

    render_data result
  end

  def create
    register_pmachine params
  end

  def new
    register_pmachine params
  end

  def edit
  end

  def show
  end

  def update
  end

  def destroy
  end

private

  def register_pmachine params
    if params[:ip] # check if ip is provided
      pmachine = Pmachine.new
      pmachine.ip = params[:ip]
      pmachine.port = params[:port] if params[:port]  # could specify pmachine service port
      pmachine_addr = "http://#{pmachine.ip}:#{pmachine.port}"

      pmachines_on_this_ip = Pmachine.find_all_by_ip params[:ip]
      pmachines_on_this_ip.each do |pmachine_on_this_ip|
        if pmachine.port == pmachine_on_this_ip.port
          render_success "Pmachine at #{pmachine_addr} is already added!" # already registered, not error
          return
        end
      end

      # check connection, and do simple authentication
      begin
        reply = RestClient.get "#{pmachine_addr}/misc/hi"
        if reply != "HI"
          render_failure "Failed to authenticate by 'hi' on '#{pmachine_addr}', check if it is running as an worker!"
          return
        end

        reply = RestClient.get "#{pmachine_addr}/misc/whoami"
        if reply != "Worker"
          render_failure "Failed to authenticate by 'whoami' on '#{pmachine_addr}', check if it is running as an worker!"
          return
        end

        #and then update all settings for that pmachine
        Setting.all.each do |setting|
          RestClient.post "#{pmachine_addr}/settings/edit.json", :key => setting.key, :value => setting.value
        end

      rescue Errno::EHOSTUNREACH => e
        render_failure "Failed to connect '#{pmachine_addr}'!"
        return
      rescue Errno::ECONNREFUSED => e
        render_failure "Connection to '#{pmachine_addr}' was refused!"
        return
      rescue Errno::ETIMEDOUT => e
        render_failure "Request to '#{pmachine_addr}' timeout!"
        return
      rescue RestClient::ResourceNotFound => e
        render_failure "Failed to authenticate '#{pmachine_addr}', check if it is running as an worker!"
        return
      rescue RestClient::RequestTimeout
        render_failure "Requet to '#{pmachine_addr}' timeout!"
        return
      end

      if pmachine.save
        render_success "Successfully added new pmachine with ip=#{params[:ip]}, port=#{pmachine.port}."
      else  # save failure
        render_failure "Failed to save data into database!"
      end
    else
      render_failure "Please provide the ip of new pmachine!"
    end
  end

end
