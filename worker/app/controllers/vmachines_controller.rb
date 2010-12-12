# Controller for virtual machines model.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'xmlsimple'

class VmachinesController < ApplicationController

public
  # Show a listing of all VM
  #
  # Since::     0.3
  def index
    doms_list = []
    Vmachine.all_domains.each do |dom|
      dom_info = {}

      # extract info by using "send"
      ["name", "uuid"].each do |property|
        dom_info[property] = dom.send property
      end

      begin
        xml_desc = XmlSimple.xml_in dom.xml_desc
        dom_info["vnc_port"] = xml_desc["devices"][0]["graphics"][0]["port"]
      rescue
      end

      vm_daemon_status = File.read "#{Setting.vm_root}/#{dom.name}/status"
      case dom.info.state
      when Vmachine::LIBVIRT_RUNNING
        dom_info["status"] = "Running"
      when Vmachine::LIBVIRT_BLOCK
        if vm_daemon_status == "migrating"
          dom_info["status"] = "Migrating"
        else
          dom_info["status"] = "Running"
        end
      when Vmachine::LIBVIRT_SUSPENDED
        dom_info["status"] = "Suspended"
      when Vmachine::LIBVIRT_NOT_RUNNING
        if vm_daemon_status == "preparing"
          dom_info["status"] = "Preparing"
        elsif vm_daemon_status == "saving"
          dom_info["status"] = "Saving"
        elsif vm_daemon_status == "migrating"
          dom_info["status"] = "Migrating"
        elsif vm_daemon_status == "hotbackup"
          dom_info["status"] = "Hotbackup"
        else
          dom_info["status"] = "Not running"
        end
      else
        dom_info["status"] = dom.info.state.to_s
      end
      doms_list << dom_info
    end

    reply_success "query successful!", :data => doms_list
  end

  # Render a observing page, uses VNC.
  #
  # Since::     0.3
  def observe
  end

  # Create and then start a domain.
  #
  # Since::     0.3
  def start
    action_request "start", params
  end

  def restart
    action_request "restart", params
  end

  # Destroy a domain.
  #
  # Since::     0.3
  def destroy
    action_request "destroy", params
  end

  # Shutdown a domain.
  #
  # Since::     0.3.5
  def power_off
    action_request "power_off", params
  end

  # Suspend a domain.
  #
  # Since::     0.3
  def suspend
    action_request "suspend", params
  end

  # Resume a domain.
  #
  # Since::     0.3
  def resume
    action_request "resume", params
  end

  # Modify the hda save to address.
  #
  # Since::     0.3
  def change_hda_save_to
    if valid_param? params[:name] # and valid_param? params[:hda_save_to]
      result = Vmachine.change_hda_save_to params[:name], params[:hda_save_to]
      if result == nil
        reply_failure "call to Vmachine.change_hda_save_to failed"
      elsif result[:success]
        reply_success result[:message]
      else
        reply_failure result[:message]
      end
    else
      reply_failure "Please provide 'name' and 'hda_save_to' params!"
    end
  end

  # realtime software deployment
  def add_package
    if valid_param? params[:name] and valid_param? params[:pkg_list]
      result = Vmachine.add_package params[:name], params[:pkg_list]
      if result == nil
        reply_failure "call to Vmachine.add_package failed"
      elsif result[:success]
        reply_success result[:message]
      else
        reply_failure result[:message]
      end
    else
      reply_failure "add_package: invalid params"
    end

  end

  def hotbackup_to
    if valid_param? params[:name] and valid_param? params[:hotbackup_dest]
      result = Vmachine.hotbackup_to params[:name], params[:hotbackup_dest], params[:hotbackup_src]
      if result == nil
        reply_failure "call to Vmachine.hotbackup_to failed"
      elsif result[:success]
        reply_success result[:message]
      else
        reply_failure result[:message]
      end

    else
      reply_failure "hotbackup_to: invalid params"
    end
 
  end

  # Tell vm_daemon to prepare migrate

  def live_migrate_to
    if valid_param? params[:name] and valid_param? params[:migrate_dest]
      result = Vmachine.live_migrate_to params[:name], params[:migrate_dest], params[:migrate_src]
      if result == nil
        reply_failure "call to Vmachine.live_migrate_to failed"
      elsif result[:success]
        reply_success result[:message]
      else
        reply_failure result[:message]
      end

    else
      reply_failure "live_migrate_to: invalid params"
    end
  end

  def suspend_all
    doms = []
    Vmachine.all_domains.each do |dom|
      if [Vmachine::LIBVIRT_RUNNING, Vmachine::LIBVIRT_BLOCK].include? dom.info.state
        result = Vmachine.suspend :name => dom.name
        doms << dom.name
      end
    end
    reply_success "Request sent, domains: [#{doms.join ","}]"
  end

  def resume_all
    doms = []
    Vmachine.all_domains.each do |dom|
      if dom.info.state == Vmachine::LIBVIRT_SUSPENDED
        result = Vmachine.resume :name => dom.name
        doms << dom.name
      end
    end
    reply_success "Request sent, domains: [#{doms.join ","}]"
  end

private

  # This is a helper, it triggers Vmachine's action, and replies result to user.
  #
  # Since::     0.3
  def action_request action_name, args
    begin
      result = Vmachine.send action_name, args
      if result == nil
        reply_failure "call to Vmachine.#{action_name} failed"
      elsif result[:success]
        reply_success result[:message]
      else
        reply_failure result[:message]
      end
    rescue => e
      reply_failure e.to_s
    end
  end

end

