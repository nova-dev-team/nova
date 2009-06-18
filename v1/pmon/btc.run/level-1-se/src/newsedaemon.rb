#! /usr/bin/env ruby

require 'sebus'
require 's0'
require File.join("../../shared-lib/", 'tools')
require 'thread'

Thread.abort_on_exception = true

class StorageEngineDaemon
  include Tools

  SCHED_INTERVAL = 10

  def initialize
    @request_que = Queue.new
    @th_sched = nil
    @th_dbus = nil
    @th_worker = nil
    @dbus = nil
  end

  ## communicate with dbus ##

  def start_sched
    LOGGER.debug "SED.start_sched ==>"
    _start_se_sched
  end

  def stop_sched
    LOGGER.debug "SED.stop_sched ==>"
    @th_sched['shut'] = true if @th_sched
  end

  def check_sched
    LOGGER.debug "SED.check_sched ==>"
    @th_sched != nil and @th_sched.alive?
  end

  def request_create(name, size, format)
    LOGGER.debug "==> request_create #{req}"
    # @request_que << S0.create_crt_vd_req(name, size, format)
  end

  def request_get_vdisk(vd_uuid, vm_uuid)
    LOGGER.debug "==> request_get_vdisk #{vd_uuid}, #{vm_uuid}"
    S0.use_a_vdisk vd_uuid, vm_uuid rescue ''
  end

  def request_del_vdisk(vm_uuid)
    LOGGER.debug "==> request_del_vdisk #{vm_uuid}"
    @request_que << S0.create_del_vd_req(vm_uuid)
  end

  ## control daemon ##

  def run
    _create_worker
    _register_dbus

    Signal.trap("TERM") do
      puts "Terminating..."
      stop_sched
      stop_worker
      @th_sched.kill if @th_sched
      @th_worker.kill if @th_worker
      exit
    end

    @th_dbus.join if @th_dbus
    @th_sched.join if @th_sched
    @th_worker.join if @th_worker
  end

  protected

  def stop_worker
    @th_worker['shut'] = true if @th_worker
  end

  def _register_dbus
    if @th_dbus.nil? or not @th_dbus.alive?
      @th_dbus = Thread.new do
        create_se_bus { |bus|
          bus.join_to self
          @dbus = bus
        }
      end
    end
  end

  def _start_se_sched
    LOGGER.debug 'sed._start_se_sched ==> '
    if @th_scheduler.nil? or not @th_scheduler.alive?
      # create scheduler thread
      @th_sched = Thread.new do
        until Thread.current['shut'] do
          if @request_que.empty?
            reqs = S0.create_clone_reqs
            reqs.each do |req|
              @request_que << req
              LOGGER.debug "sched >> #{req}"
            end
          end
          LOGGER.debug 'se sched z z Z'
          sleep SCHED_INTERVAL
        end # until
        LOGGER.debug 'sed : quit sched thread.'
      end # thread
    end # if
  end

  def _create_worker
    if @th_worker.nil? or not @th_worker.alive?
      # create worker thread
      @th_worker = Thread.new do
        until Thread.current['shut'] do
          req = @request_que.pop
          LOGGER.debug "worker>> #{req}"

          case req.req_type
          when 'create'
            S0.process() do
              @dbus.start_create req.vd_uuid
              S0.process_create req
              @dbus.finish_create req.vd_uuid
            end
          when 'clone'
            S0.process() do
              @dbus.start_clone req.vd_uuid
              S0.process_clone req rescue LOGGER.error("worker ==> fail to process_clone #{req}")
              @dbus.finish_clone req.vd_uuid
            end
          when 'delete'
            S0.process() do
              @dbus.start_delete req.vm_uuid
              S0.process_delete req rescue LOGGER.error("worker ==> fail to process_delete #{req}")
              @dbus.finish_delete req.vm_uuid
            end
          else
            LOGGER.debug 'unknown request type'
            LOGGER.debug req.req_type
            LOGGER.debug req.to_s
          end
        end
      end
    end
  end

end

sed = StorageEngineDaemon.new
sed.run

