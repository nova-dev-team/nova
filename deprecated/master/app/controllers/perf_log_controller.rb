# Performance log controller.
#
# Since::   0.3

class PerfLogController < ApplicationController

  # only root and admin users could view the monitor info
  before_filter :root_or_admin_required

  # List all the pmachines.
  # We provide this api for admin access.
  # This api is not like the Pmachine.list function (in pmachines_controller.rb), which is only available for root.
  #
  # Since::   0.3
  def list_pm
    list_data = []
    Pmachine.all.each do |pm|
      pm_data = {
        :id => pm.id,
        :ip => pm.ip,
        :hostname => pm.hostname,
        :status => pm.status,
        :vm_capacity => pm.vm_capacity
      }
      vm_preparing = 0
      vm_failure = 0
      vm_running = 0
      pm.vmachines.each do |vm|
        if vm.status.downcase == "preparing"
          vm_preparing += 1
        elsif vm.status.downcase == "failure"
          vm_failure += 1
        elsif vm.status.downcase == "running"
          vm_running += 1
        end
      end
      pm_data[:vm_preparing] = vm_preparing
      pm_data[:vm_failure] = vm_failure
      pm_data[:vm_running] = vm_running
      list_data << pm_data
    end
    reply_success "query successful!", :data => list_data
  end

  # Reply an array of perf logs.
  # Optional args:
  # * time: from when should the perf log be retrieved
  # * pm_ip: the pmachine ip, of which the perf log will be retireved
  #
  # Since::   0.3
  def show
    start_time = nil
    start_time = params[:time] if valid_param? params[:time]
    pm_id = nil
    if valid_param? params[:pm_ip]
      pm = Pmachine.find_by_ip params[:pm_ip]
      if pm != nil
        pm_id = pm.id
      end
    end

    if start_time != nil and pm_id != nil
      perf_data = PerfLog.find(:all, :conditions => ["time >= ? and pmachine_id = ?",start_time, pm_id], :order => "time DESC")
    elsif start_time != nil and pm_id == nil
      perf_data = PerfLog.find(:all, :conditions => ["time >= ?", start_time], :order => "time DESC")
    elsif start_time == nil and pm_id != nil
      perf_data = PerfLog.find(:all, :conditions => ["pmachine_id = ?", pm_id], :order => "time DESC")
    else
      # start_time == nil and pm_id == nil
      perf_data = PerfLog.find(:all, :order => "time DESC")
    end

    if start_time == nil and perf_data.size > 0
      start_time = perf_data.first[:time]
    end

    reply_data = []
    perf_data.each do |perf|
      reply_data << {
        :dAvail => perf.dAvail,
        :time => perf.time,
        :memTotal => perf.memTotal,
        :pmachine_id => perf.pmachine_id,
        :memFree => perf.memFree,
        :CPU => perf.CPU,
        :dSize => perf.dSize,
        :Tran => perf.Tran,
        :Rece => perf.Rece
      }
    end

    reply_success "Query successful!", :start_time => start_time, :pm_id => pm_id, :pm_ip => params[:pm_ip], :data => reply_data
  end
end
