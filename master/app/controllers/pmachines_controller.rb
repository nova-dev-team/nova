require 'rubygems'
require 'rest_client'

class PmachinesController < ApplicationController

  before_filter :require_admin_privilege

  def index
    result = []
    Pmachine.all.each do |pmachine|
      result << {
        :addr => pmachine.addr,
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
      port = params[:port] || "3000" # default port is 3000
      addr = "#{params[:ip]}:#{port}"
      if (Pmachine.register :addr => addr, :vnc_first => params[:vnc_first], :vnc_last => params[:vnc_last])
        render_success "Successfully registered pmachine: #{addr}."
      else
        render_failure "Failed to register pmachine: #{addr}."
      end
    else
      render_failure "Please provide the ip of new pmachine!"
    end
  end

end
