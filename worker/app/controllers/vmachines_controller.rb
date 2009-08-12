require 'rubygems'
require 'json'
require 'libvirt'
require 'pp'
require 'xmlsimple'
require 'uuidtools'

class VmachinesController < ApplicationController

  include VmachinesHelper

public

  STATE_RUNNING = 1
  STATE_SUSPENDED = 3

  @@virt_conn = Libvirt::open("qemu:///system")

  # list all vmachines, and show their status
  def index
    params[:show_active] ||= true
    params[:show_inactive] ||= false

    doms_info = []
    all_domains = []

    if params[:show_inactive] # inactive domains are listed by name
      @@virt_conn.list_defined_domains.each do |dom_name|
        begin
          all_domains << @@virt_conn.lookup_domain_by_name(dom_name)
        rescue
          next # ignore error, go on with next one
        end
      end
    end

    if params[:show_active] # active domains are listed by id
      @@virt_conn.list_domains.each do |dom_id|
        begin
          all_domains << @@virt_conn.lookup_domain_by_id(dom_id)
        rescue
          next
        end
      end
    end

    all_domains.each do |dom|
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

  # show detail info of a machine
  def detail_info
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    rescue # would fail if dom not found
      alert_domain_not_found
      return
    end
    
    result = {
      :success => true,
      :message => "Query succeeded.",
      :name => dom.name,
      :uuid => dom.uuid
      # TODO add more detail info
    }

    respond_to do |accept|
      accept.json {render :json => result}
      accept.json {render :text => result}
    end
  end

  # create & start an domain
  def start

    # those are default parameters
    default_params = {
      "emulator" => "kvm",
      "name" => "dummy_vm",
      "vcpu" => 2,
      "mem_size" => 128,
      "uuid" => UUIDTools::UUID.random_create.to_s
    }
    
    # merge default parameters into real params, if corresponding item does not exist
    default_params.each do |key, value|
      params[key] ||= value
    end

    # emit virtual machine xml specification
    dom_xml = VmachinesHelper::Helper.emit_xml_spec params
    logger.debug "*** [create] Vmachine XML Specfication"
    logger.debug dom_xml

    begin
      # create vmachine domain, could fail
      dom = @@virt_conn.define_domain_xml dom_xml
      dom.create
      render_success "Successfully created vmachine domain, name=#{dom.name}, UUID=#{dom.uuid}."
    rescue
      # report error to calling server
      
      # check if the domain already exists
      begin
        @@virt_conn.lookup_domain_by_name params[:name]
        render_failure "Failed to create vmachine domain! Domain name #{params[:name]} already used!"
        return
      rescue
        # domain name not used, do nothing
      end

      render_failure "Failed to create vmachine domain!"
    end

  end

  # stop and destroy an domain
  def destroy
    libvirt_action "destroy", params
  end

  def reboot
    libvirt_action "reboot", params
  end

  def suspend
    libvirt_action "suspend", params
  end

  def resume
    libvirt_action "resume", params
  end

private

  # general action for libvirt
  def libvirt_action action_name, params
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    rescue
      alert_domain_not_found
      return
    end

    begin
      dom.send action_name
      render_success "Successfully #{action_name}ed domain, name=#{dom.name}, UUID=#{dom.uuid}."
    rescue
      render_failure "Failed to #{action_name} domain, UUID=#{dom.uuid}!"
    end
  end


  def render_success message
    render_result :success => true, :message => message
  end

  def render_failure message
    render_result :success => false, :message => message
  end

  # reply to client
  def render_result result
    logger.debug "*** [reply] success=#{result[:success]}, message=#{result[:message]}"
    respond_to do |accept|
      accept.json {render :json => result}
      accept.html {render :text => result.pretty_inspect}
    end
  end

  def alert_domain_not_found params
    logger.debug "*** [error] domain not found, UUID=#{params[:uuid]}"
    render_result :success => false, :message => "Domain not found, check your UUID! (You requested for UUID=#{params[:uuid]})"
  end

  def state_has_vnc? state_id
    return [STATE_RUNNING, STATE_SUSPENDED].include? state_id
  end

end

