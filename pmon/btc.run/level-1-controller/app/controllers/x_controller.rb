
require 'rubygems'
require 'xmlsimple'
require 'libvirt'
require 'pp'
require File.join('../shared-lib/' + "utils")

class XController < ApplicationController
  include XHelper
  include Utils

  skip_before_filter :verify_authenticity_token

  def initialize
    @conn = Libvirt::open("qemu:///system")
  end

  def list
    l = []
    @list_of_vm = []
    @conn.list_defined_domains.each {|id|
      l << @conn.lookup_domain_by_name(id) }
    l.each do |d|
      h = {}
      h[:name]  = d.name
      h[:uuid]  = d.uuid
      h[:state] = "#{ d.id} / #{d.info.state}"
      h[:cpu_time] = d.info.cpu_time
      @list_of_vm << h
    end

    l = []
    @conn.list_domains.each {|id|
      l << @conn.lookup_domain_by_id(id) }
    l.each do |d|
      h = {}
      h[:name]  = d.name
      h[:uuid]  = d.uuid
      h[:state] = "#{ d.id} / #{d.info.state}"
      h[:cpu_time] = d.info.cpu_time
      @list_of_vm << h
    end

    # todo : will be removed
    #@newvm = VmReq.new :format => 'none', :data => nil

    respond_to do |accepts|
      accepts.html
      accepts.json {render :json => @list_of_vm }
    end
  end

  def start
    begin
      d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} check fail, vm is not found.", :status => :not_found
      return
    end

    if [0,5,6].include? d.info.state
      # todo: check vnc port should be -1.
      d.create
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} start ok"}
        accepts.json {render :json => d.info}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} start fail, vm(#{d.info.state}) is not stop.", :status => :conflict}
        accepts.json {render :json => d.info.to_json, :status => :conflict}
      end
    end
  end

  def stop
    begin
      d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} check fail, vm is not found.", :status => :not_found
      return
    end

    if [1,2,3].include? d.info.state
      d.shutdown
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} stop ok"}
        accepts.json {render :json => d.info}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} stop fail, vm(#{d.info.state}) is not running.", :status => :conflict}
        accepts.json {render :json => d.info, :status => :conflict}
      end
    end
  end

  def suspend
    begin
      d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} check fail, vm is not found.", :status => :not_found
      return
    end

    if [1,2].include? d.info.state
      d.suspend
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} suspend ok"}
        accepts.json {render :json => d.info}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} suspend fail, vm(#{d.info.state}) is not running.", :status => :conflict}
        accepts.json {render :json => d.info, :status => :conflict}
      end
    end
  end

  def resume
    begin
      d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} check fail, vm is not found.", :status => :not_found
      return
    end

    if [3].include? d.info.state
      d.resume
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} resume ok"}
        accepts.json {render :json => d.info}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "#{params[:id]} resume fail, vm(#{d.info.state}) is not suspend.", :status => :conflict}
        accepts.json {render :json => d.info, :status => :conflict}
      end
    end
  end

  # 建立虚拟机并申请空间等资源
  #
  # 出错的返回状态:
  #   - 409 Conflict, 这个是重复的请求或资源不够。
  #   - 400 Bad Request, 这个一般是Client编程上的错误。
  #   - 500 Internal Server Error, 未知错误。
  def create
    # vm = Vm.new :format => params[:vm][:format], :data => params[:vm][:define]
    #d = @conn.define_domain_xml vm.getxml


    puts params[:define]

    logger.debug "X 1 >> parse xml"
    #    xml = REXML::Document.new params[:vm][:define]
    xml = REXML::Document.new params[:define]
    uuid = REXML::XPath.first xml, '/domain/uuid/text()'

    logger.debug "X 2 >> create request"
    req =  Request.new
    req.kind = 'create'
    req.uuid = uuid.to_s
    req.save!

    logger.debug "X 3 >> prepare storage"
    storage_ready = false
    prepare_storage req, xml
    storage_ready = true

    pp xml.to_s

    logger.debug "X 4 >> define domain"
    d = @conn.define_domain_xml xml.to_s

    respond_to do |accepts|
      accepts.html {render :text => "create the #{d.uuid}, ok"}
      accepts.json {render :json => d.uuid}
    end

  rescue SQLite3::SQLException, S0::S0Error => e
    logger.error ">>>CREATE>>>Conflict requests."
    logger.error e
    logger.error req
    logger.error xml
    reset_storage_ready uuid.to_s if storage_ready
    respond_to do |accepts|
      accepts.html {render :text => "create fail, because of #{e.message}", :status => :conflict}
      accepts.json {render :json => "create fail, because of #{e.message}", :status => :conflict}
    end
  rescue REXML::ParseException, Libvirt::DefinitionError => e
    logger.error ">>>CREATE>>>FATAL ERROR CAPTURED>>> Bad Request"
    logger.error e
    reset_storage_ready uuid.to_s if storage_ready
    respond_to do |accepts|
      accepts.html {render :text => "Don\'t repeat the request'. Because of #{e.message}", :status => :bad_request}
      accepts.json {render :json => "Don\'t repeat the request. Because of #{e.message}", :status => :bad_request}
    end
  rescue => e
    logger.error ">>>CREATE>>>FATAL ERROR CAPTURED>>> Unkown Error."
    logger.error e
    reset_storage_ready uuid.to_s if storage_ready
    respond_to do |accepts|
      accepts.html {render :text => "Unknown error, #{e.message}", :status => :internal_server_error}
      accepts.json {render :json => "Unknown error, #{e.message}", :status => :internal_server_error}
    end
  ensure
    req.destroy unless req.nil?
  end

  # 察看一个虚拟机的状态：
  #    check id => domain::info
  #
  # libvirt定义的虚拟机的状态：
  #   * VIR_DOMAIN_NOSTATE  =   0  : no state
  #   * VIR_DOMAIN_RUNNING  =   1 : the domain is running
  #   * VIR_DOMAIN_BLOCKED  =   2 : the domain is blocked on resource
  #   * VIR_DOMAIN_PAUSED =   3 : the domain is paused by user
  #   * VIR_DOMAIN_SHUTDOWN =   4 : the domain is being shut down
  #   * VIR_DOMAIN_SHUTOFF  =   5 : the domain is shut off
  #   * VIR_DOMAIN_CRASHED  =   6 : the domain is crashed
  def check
    begin
      @d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} check fail, vm is not found.", :status => :not_found
      return
    end

    domain = XmlSimple.xml_in @d.xml_desc
    @port = domain['devices'][0]['graphics'][0]['port']

    respond_to do |accepts|
      accepts.html
      accepts.json {render :json => @d.info.to_json}
    end
    return
  end

  def destroy
    begin
      d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} destroy fail, vm is not found.", :status => :not_found
      return
    end

    if [1,2,3,4].include? d.info.state
      d.destroy
    end

    call_bus('/cn/org/btc/StorageEngine') {|bus| bus.delete params[:id] }

    d.undefine
    d.free

    respond_to do |accepts|
      accepts.html {render :text => "#{params[:id]} destroy ok"}
      accepts.json {render :nothing}
    end
  end

  def vnc_port
    begin
      @d = @conn.lookup_domain_by_uuid params[:id]
    rescue
      render :text => "#{params[:id]} check fail, vm is not found.", :status => :not_found
      return
    end

    domain = XmlSimple.xml_in @d.xml_desc
    port = domain['devices'][0]['graphics'][0]['port']

    respond_to do |accepts|
      accepts.html {render :text => port}
      accepts.json {render :json => port}
    end
    return
  end
end
