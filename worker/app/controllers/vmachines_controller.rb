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

      # get the vnc port
      if dom.info.state == Vmachine::LIBVIRT_RUNNING or dom.info.state == Vmachine::LIBVIRT_SUSPENDED
        # only these 2 state has vnc port
        xml_desc = XmlSimple.xml_in dom.xml_desc
        dom_info["vnc_port"] = xml_desc["device"][0]["graphics"][0]["port"]
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

  # Destroy a domain.
  #
  # Since::     0.3
  def destroy
    action_request "destroy", params
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

