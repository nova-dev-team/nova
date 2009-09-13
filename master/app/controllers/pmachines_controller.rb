require 'rubygems'
require 'rest_client'

class PmachinesController < ApplicationController

  before_filter :require_admin_privilege

  def index
    result = []
    Pmachine.all.each do |pmachine|
      result << {
        :ip => pmachine.ip,
        :port => pmachine.port,
        :vnc_first => pmachine.vnc_first,
        :vnc_last => pmachine.vnc_last
      }
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
    # require vnc port information
    if params[:vnc_first] == nil || params[:vnc_last] == nil
      render_failure "Please provide VNC port range!"
      return
    end

    if PmachinesHelper::Helper.vnc_collision? params[:vnc_first], params[:vnc_last]
      render_failure "VNC ports range #{params[:vnc_first]}-#{params[:vnc_last]} has already been used! You should try this range: #{PmachinesHelper::Helper.vnc_recommendation}"
      return
    end

    if params[:ip] # check if ip is provided
      pmachine = Pmachine.new
      port = params[:port] | 3000 # default port is 3000
      pmachine.addr = "#{params[:ip]}:#{port}"
      pmachine.vnc_first = params[:vnc_first]
      pmachine.vnc_last = params[:vnc_last]
      pmachine_addr = "http://#{pmachine.addr}"

      if Pmachine.find_by_addr pmachine.addr
        render_success "Pmachine at #{pmachine_addr} is already added!" # already registered, not error
        return
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
