# Model for physical machines.
#
# Author::  Santa Zhang (mailto:santa1987@gmail.com)
# Since::    0.3

require 'rubygems'
require 'rest_client'
require 'pp'
require 'json'
require 'timeout'
require "worker_proxy"

class Pmachine < ActiveRecord::Base

  has_many :vmachines

  # A helper function, returns the root url of the worker module.
  #
  # Since::   0.3
  def root_url
    conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    return "http://#{self.ip}:#{conf["worker_port"]}"
  end

  # Start a VM. A very simple schedule process will be taken.
  # The hosting pmachine will be returned.
  # On sched error, nil will be returned.
  #
  # Since::   0.3
  def Pmachine.start_vm vm
    logger.info "[pm.sched] starting sched"
    sorted_pm = Pmachine.all.sort {|pm1, pm2| pm1.vmachines.length <=> pm2.vmachines.length}
    logger.info "[pm.sched] sorting done"
    sched_pm = nil
    sorted_pm.each do |pm|
      if pm.vmachines.length < pm.vm_capacity
        sched_pm = pm
        break
      end
    end
    logger.info "[pm.sched] target found"
    if sched_pm != nil
      conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
      wp = WorkerProxy.new "#{sched_pm.ip}:#{conf["worker_port"]}"
      logger.info "[pm.info] worker proxy created"
      # TODO start vm
      sched_pm.vmachines << vm
      sched_pm.save
      vm.save
      logger.info "[pm.info] safe round"
    end
    return sched_pm
  end

end
