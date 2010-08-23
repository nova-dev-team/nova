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
  has_many :perf_logs

  # A helper function, returns the root url of the worker module.
  #
  # Since::   0.3
  def root_url
    conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    return "http://#{self.ip}:#{conf["worker_port"]}"
  end

  # Create a worker proxy for the physical machine.
  #
  # Since::   0.3
  def worker_proxy
    conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    wp = WorkerProxy.new "#{self.ip}:#{conf["worker_port"]}"
    return wp
  end

  # Start a VM. A very simple schedule process will be taken.
  # The hosting pmachine will be returned.
  # On sched error, nil will be returned.
  #
  # Since::   0.3
  def Pmachine.start_vm vm
    logger.info "[pm.sched] starting sched"
    all_not_retired = Pmachine.all.select {|pm| pm.status == "working"}
    logger.info "[pm.sched] machines not retired: count=#{all_not_retired.size}"
    sorted_pm = all_not_retired.sort {|pm1, pm2| pm1.vmachines.length <=> pm2.vmachines.length}
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
      sched_pm.vmachines << vm
      sched_pm.save
      vm.save
      logger.info "[pm.info] starting the VM #{vm.name}"
      nodelist = (vm.vcluster.vmachines.collect {|v| "#{v.ip} #{v.hostname}"}).join ","
      logger.info "[pm.info] node list is: #{nodelist}"

      should_use_hvm = false
      if conf["hypervisor"] == "xen"
        vd = Vdisk.find_by_file_name vm.hda
        if vd.os_family == "windows"
          should_use_hvm = true
        end
      end

      xen_kernel = nil
      xen_initrd = nil
      xen_hda_dev = nil

      real_soft_list = []
      vm.soft_list.split(",").each do |item|
        item = item.strip
        next if item == ""
        if item.start_with? "__kernel="
          xen_kernel = item[9..-1]
        elsif item.start_with? "__initrd="
          xen_initrd = item[9..-1]
        elsif item.start_with? "__hda_dev="
          xen_hda_dev = item[10..-1]
        else
          real_soft_list << item
        end
      end

      # start vm
      start_vm_params = {
        :uuid => vm.uuid,
        :name => vm.name,
        :cpu_count => vm.cpu_count,
        :memory_size => vm.memory_size,
        :use_hvm => should_use_hvm,
        :vdisk_fname => vm.hda,
        :packages => real_soft_list.join(","),
        :cluster_name => vm.vcluster.cluster_name,
        :nodelist => nodelist,
        :ip => vm.ip,
        :submask => Setting.vm_subnet_mask,
        :gateway => Setting.vm_gateway,
        :dns => Setting.vm_dns_server
      }

      start_vm_params[:kernel] = xen_kernel if xen_kernel != nil
      start_vm_params[:initrd] = xen_initrd if xen_initrd != nil
      start_vm_params[:hda_dev] = xen_hda_dev if xen_hda_dev != nil

      ret = wp.start_vm start_vm_params

      if ret == nil
        logger.error "[pm.error] failed to start vm"
      else
        logger.info "[pm.info] started vm! raw reply is: #{ret}"
      end

      logger.info "[pm.info] safe round"
    end
    return sched_pm
  end

end
