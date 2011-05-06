class Log < ActiveRecord::Base


  # get the logs which is written after params[:time] in log file, if you don't give the time we will show all logs
  # params[:path] is the log file path
  # use binary serach to seach the first log to show
  def Log.get_tail params
    ori_logs = []
    tail_counter = 10
    IO.popen("tail -n #{tail_counter} #{params[:path]}") do |pipe|
      pipe.each_line do |line|
        ori_logs << line.strip
      end
    end
    index = 0
    logs = []
    while index < ori_logs.size

      time_value = ori_logs[index].slice(6, 15)
      time_value = time_value.delete("-")

      if params[:time] != nil and params[:time] != ""
        begin
          if Time.parse(time_value) < Time.parse(params[:time])
            index += 1
            next
          end
        rescue
          break
        end
      end

      cpu_value = get_value({:log => ori_logs[index], :key => "CPU", :offset => 5})
      memTotal_value = get_value({:log => ori_logs[index], :key => "memTotal", :offset => 10})
      memFree_value = get_value({:log => ori_logs[index], :key => "memFree", :offset => 9})
      dSize_value = get_value({:log => ori_logs[index], :key => "dSize", :offset => 7})
      dAvail_value = get_value({:log => ori_logs[index], :key => "dAvail", :offset => 8})
      rece_value = get_value({:log => ori_logs[index], :key => "Rece", :offset => 6})
      tran_value = get_value({:log => ori_logs[index], :key => "Tran", :offset => 6})

      logs << { :Time => time_value, :CPU => cpu_value, :memTotal => memTotal_value, \
        :memFree => memFree_value, :dSize => dSize_value, :dAvail => dAvail_value, :Rece => rece_value, :Tran => tran_value}

      index += 1
    end
    logs
  end

  def Log.get_value params
    log = params[:log]
    log_pos_b = log.index(params[:key]) + params[:offset]
    log = log.slice(log_pos_b, log.size - log_pos_b)
    log_pos_e = log.index(" ")
    if(log_pos_e == nil)
      log_pos_e = log.size - 1
    end
    log.slice(0, log_pos_e)
  end

end
