require 'rubygems'
require 'json'
require 'libvirt'
require 'pp'
require 'xmlsimple'
require 'uuidtools'
require 'fileutils'

class VmachinesController < ApplicationController

public

  # libvirt states
  STATE_RUNNING = 1
  STATE_SUSPENDED = 3
  STATE_NOT_RUNNING = 5

  def index
    list
  end

  # list all vmachines, and show their status
  # TODO deprecate this function
  def list
    params[:show_active] ||= "true"
    params[:show_inactive] ||= "false"
    doms_info = []
    Vmachine.all_domains.each do |dom|
      dom_info = {}

      ["name", "uuid", "info"].each do |property|
        dom_info[property] = dom.send property
      end

      # vnc port
      if state_has_vnc? dom.info.state
        xml_desc = XmlSimple.xml_in dom.xml_desc
        dom_info["vnc_port"] = xml_desc['devices'][0]['graphics'][0]['port']
      end

      doms_info << dom_info 
    end

    respond_to do |accept|
      accept.json {render :json => doms_info}
      accept.html {render :text => doms_info.to_json}
    end
  end

  # For debug purpose, create a domain, but do not start it.
  #
  # Since::     0.3
  def create
    # TODO disable this function in production code.
    begin
      Vmachine.define params
      reply_success "success!"
    rescue => e
      reply_failure e.to_s
    end
  end

  # create & start an domain
  def start
    Vmachine.default_params.each do |key, value|
      params[key] = value unless valid_param? params[key]
    end

    action_request "start", params
  end

  # stop and destroy an domain
  def destroy
    action_request "destroy", params[:uuid]
  end

  def suspend
    action_request "suspend", params[:uuid]
  end

  def resume
    action_request "resume", params[:uuid]
  end

private

  def action_request action_name, args
    result = Vmachine.send action_name, args
    render_result result[:success], result[:message]
  end

end

