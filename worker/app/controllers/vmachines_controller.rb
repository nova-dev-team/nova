require 'rubygems'
require 'json'
require 'libvirt'
require 'pp'
require 'xmlsimple'
require 'uuidtools'
require 'fileutils'

class VmachinesController < ApplicationController

  include VmachinesHelper

public

  # libvirt states
  STATE_RUNNING = 1
  STATE_SUSPENDED = 3
  STATE_NOT_RUNNING = 5

  @@virt_conn = VmachinesHelper::Helper.virt_conn

  # show the web UI
  def index
    render :template => 'vmachines/index.html.erb', :layout => 'default'
  end

  # list all vmachines, and show their status
  def list
    params[:show_active] ||= "true"
    params[:show_inactive] ||= "false"

    doms_info = []
    all_domains = []

    if params[:show_inactive] == "true" # inactive domains are listed by name
      @@virt_conn.list_defined_domains.each do |dom_name|
        begin
          all_domains << @@virt_conn.lookup_domain_by_name(dom_name)
        rescue
          next # ignore error, go on with next one
        end
      end
    end

    if params[:show_active] == "true" # active domains are listed by id
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
      alert_domain_not_found params
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
      :arch => "i686",
      :emulator => "kvm", # ENHANCE currently we only support KVM
      :name => "dummy_vm",
      :vcpu => 1,
      :mem_size => 128,
      :uuid => UUIDTools::UUID.random_create.to_s,
      :hda => "vdisk.qcow2",
      :hdb => "", # this is optional, could be "" (means no such a device)
      :cdrom => "", # this is optional, could be "" (means no such a device)
      :boot_dev => "hda", # hda, hdb, cdrom
      :vnc_port => -1   # setting vnc_port to -1 means libvirt will automatically set the port
    }
    
    # merge default parameters into real params, if corresponding item does not exist
    default_params.each do |key, value|
      if params[key] == nil or params[key] == ""
        params[key] = value
      end
    end
    
    begin
      xml_desc = VmachinesHelper::Helper.emit_xml_desc params
      logger.debug "*** [create] Vmachine XML Specfication"
      logger.debug xml_desc

      dom = @@virt_conn.define_domain_xml xml_desc 
    rescue Exception => e
      # report error to calling server
      
      # check if the domain already exists
      begin
        @@virt_conn.lookup_domain_by_name params[:name]
        render_failure "Failed to create vmachine domain! Domain name #{params[:name]} already used! Raw error: #{e.pretty_inspect}"
        return
      rescue
        # domain name not used, do nothing
      end

      render_failure "Failed to create vmachine domain! Raw error: #{e.pretty_inspect}"
      return
    end

    begin
      # creation is put into job queue, handled by backgroundrb
      MiddleMan.worker(:vmachines_worker).async_do_start(:args => params)
      render_success "Successfully created vmachine domain, name=#{dom.name}, UUID=#{dom.uuid}. It is being started right now."
    rescue
      render_failure "Failed to add creation request into job queue!"
    end

  end

  # stop and destroy an domain
  def destroy
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
      dom_name = dom.name
    rescue
      alert_domain_not_found params
      return
    end

    begin
      dom.destroy if state_can_destroy? dom.info.state
    rescue
      render_failure "Failed to destroy domain, UUID=#{params[:uuid]}!"
      return
    end

    # TODO recollect resources, such as vdisks, cdrom-iso, and network
    begin
      cleanup_args = {
        :vmachines_root => Setting.vmachines_root,
        :vmachine_name => dom_name
      }
      MiddleMan.worker(:vmachines_worker).async_do_cleanup(:args => cleanup_args)
      dom.undefine
      render_success "Successfully destroyed virtual machine, name=#{dom_name}, UUID=#{params[:uuid]}."
    rescue
      render_failure "Failed to recollect allocated resources, name=#{dom_name}, UUID=#{params[:uuid]}"
    end

  end

  # TODO check if this will work
  def reboot
    libvirt_action "reboot", params
  end

  def suspend
    libvirt_action "suspend", params
  end

  def resume
    libvirt_action "resume", params
  end

  def ensure_cached
  end

  def cache_status
  end

private

  # general action for libvirt
  def libvirt_action action_name, params
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    rescue
      alert_domain_not_found params
      return
    end

    begin
      dom.send action_name
      render_success "Successfully #{action_name}ed domain, name=#{dom.name}, UUID=#{dom.uuid}."
    rescue
      render_failure "Failed to #{action_name} domain, UUID=#{dom.uuid}!"
    end
  end

  def alert_domain_not_found params
    logger.debug "*** [error] domain not found, UUID=#{params[:uuid]}"
    render_result :success => false, :message => "Domain not found, check your UUID! (You requested for UUID=#{params[:uuid]})"
  end

  def state_can_destroy? state_id
    return [STATE_RUNNING, STATE_SUSPENDED].include? state_id
  end

  def state_has_vnc? state_id
    return [STATE_RUNNING, STATE_SUSPENDED].include? state_id
  end

end

