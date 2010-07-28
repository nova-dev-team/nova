# Performance log controller.
#
# Since::   0.3

class PerfLogController < ApplicationController

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
      perf_data = PerfLog.find(:all, :conditions => ["time >= ? and pmachine_id = ?",start_time, pm_id])
    elsif start_time != nil and pm_id == nil
      perf_data = PerfLog.find(:all, :conditions => ["time >= ?", start_time])
    elsif start_time == nil and pm_id != nil
      perf_data = PerfLog.find(:all, :conditions => ["pmachine_id = ?", pm_id])
    else
      # start_time == nil and pm_id == nil
      perf_data = PerfLog.all
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
