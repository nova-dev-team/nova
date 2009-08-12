require 'rubygems'
require 'json'
require 'libvirt'
require 'pp'
require 'uuidtools'

class VmachinesController < ApplicationController

  include VmachinesHelper

public

  @@virt_conn = Libvirt::open("qemu:///system")

  # TODO list all vmachines, and show their status
  def index
    doms = @@virt_conn.list_defined_domains
    render_result [doms, @@virt_conn.list_defined_domains]
  end

  # TODO show detail info of a machine
  def detail_info
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    rescue # would fail if dom not found
      render_result "Domain not found, check your UUID! (You requested for UUID=#{params[:uuid]})"
      return
    end
    render_result dom
  end

  # TODO create & start an domain
  def start
    params = {
      :emulator => "kvm",
      :name => "test-vm",
      :vcpu => 1,
      :mem_size => 512,
      :uuid => UUIDTools::UUID.random_create.to_s
    }

    dom_xml = VmachinesHelper::Helper.emit_xml_spec params
    logger.debug "*** [create] Vmachine XML Specfication"
    logger.debug dom_xml

    begin
      dom = @@virt_conn.define_domain_xml dom_xml
      dom.create
      render_result :success => true, :message => "Successfully created vmachine domain."
    rescue
      render_result :success => false, :message => "Failed to create vmachine domain, check your xml specification!"
    end

  end

  # TODO stop and destroy an domain
  def stop
    dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    render_result dom
  end

  # TODO
  def reboot
    dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    render_result dom
  end

  # TODO
  def suspend
    dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    render_result dom
  end

  # TODO
  def resume
    dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    render_result dom
  end

private

  # reply to client
  def render_result result
    logger.debug "*** [reply] success=#{result[:success]}, message=#{result[:message]}"
    respond_to do |accept|
      accept.json {render :json => result}
      accept.html {render :text => result.pretty_inspect}
    end
  end

end

