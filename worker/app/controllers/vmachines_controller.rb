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

  def index
    list
  end

  # list all vmachines, and show their status
  # TODO deprecate this function
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
      :depend => "", # additional dependency on COW disks
      :boot_dev => "hda", # hda, hdb, cdrom
      :vnc_port => -1,   # setting vnc_port to -1 means libvirt will automatically set the port
      :mac => "11:22:33:44:55:66"  # mac is required
    }

    default_params.each do |key, value|
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

  def state_can_destroy? state_id
    return [STATE_RUNNING, STATE_SUSPENDED].include? state_id
  end

  def state_has_vnc? state_id
    return [STATE_RUNNING, STATE_SUSPENDED].include? state_id
  end

end

