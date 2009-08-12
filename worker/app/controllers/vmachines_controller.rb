require 'rubygems'
require 'json'
require 'libvirt'
require 'pp'

class VmachinesController < ApplicationController

public

  @@virt_conn = Libvirt::open("qemu:///system")

  # TODO list all vmachines, and show their status
  def index
    doms = @@virt_conn.list_defined_domains
    render_result doms
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

  # TODO Start an image, non-blocking, and should lock image files
  def start
    dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
    render_result dom
  end

  # TODO
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

  def render_result result
    respond_to do |accept|
      accept.json {render :json => result}
      accept.html {render :text => result.pretty_inspect}
    end
  end

end

