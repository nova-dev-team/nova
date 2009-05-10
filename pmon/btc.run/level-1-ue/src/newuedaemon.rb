#! /usr/bin/env ruby

require 'uebus'
require 'u0'
require File.join("../../shared-lib/", 'tools')
require 'thread'

Thread.abort_on_exception = true

class UpdateEngineDaemon
  include Tools

  SCHED_INTERVAL = 10

  def initialize
    @request_que = Queue.new
    @th_dbus = nil
    @th_sched = nil
    @th_worker = nil
    @dbus = nil
  end

  ## communicate with dbus ##

  def request_download(url, priority, size)
    LOGGER.debug "==> request_download #{url}"
    U0.request_download(url, priority, size)
  end

  def start_sched
    LOGGER.debug "UED.start_sched ==>"
    _start_sched
  end

  def stop_sched
    LOGGER.debug "UED.stop_sched ==>"
    @th_sched['shut'] = true if @th_sched
  end

  def check_sched
    LOGGER.debug "UED.check_sched ==>"
    @th_sched != nil and @th_sched.alive?
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
    LOGGER.debug "ued._register_dbus ==>"

    if @th_dbus.nil? or not @th_dbus.alive?
      @th_dbus = Thread.new do
        create_ue_bus { |bus|
          bus.join_to self
          @dbus = bus
        }
      end
    end
  end

  def _start_sched
    LOGGER.debug 'uedaemon._start_sched ==> '
    if @th_scheduler.nil? or not @th_scheduler.alive?
      # create scheduler thread
      @th_sched = Thread.new do
        until Thread.current['shut'] do
          if @request_que.empty?
            reqs = U0.get_update_reqs
            reqs.each do |req|
              @request_que << req
              LOGGER.debug "sched enqueue >> #{req}"
            end
            LOGGER.debug 'ue sched z z Z'
            sleep SCHED_INTERVAL
          end # if
        end # until
        LOGGER.debug 'ued : quit sched thread.'
      end # thread
    end
  end

  def _create_worker
    if @th_worker.nil? or not @th_worker.alive?
      # create worker thread
      @th_worker = Thread.new do
        until Thread.current['shut'] do
          req = @request_que.pop
          LOGGER.debug "worker>> #{req}"

          case req.req_type
          when 'download'
            U0.process() do
              @dbus.download_progress req.url, 0
              begin
                U0.process_download req
                @dbus.download_progress req.url, 100
              rescue
                LOGGER.error("worker ==> fail to process_download #{req}")
                @dbus.download_progress req.url, -100
              end
            end
          else
            LOGGER.debug 'UED : unknown request type'
            LOGGER.debug "UED : " + req.req_type
            LOGGER.debug "UED : " + req.to_s
          end
        end
      end
    end
  end

end

sed = UpdateEngineDaemon.new
sed.run

