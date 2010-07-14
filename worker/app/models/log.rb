class Log < ActiveRecord::Base

	
  # get the logs which is written after params[:time] in log file, if you don't give the time we will show all logs
  # params[:path] is the log file path
  # use binary serach to seach the first log to show
  def Log.get_tail params
    i = 0
    file = File.new(params[:path])
    strs = file.readlines[i]
    ori_logs = []
    while strs
      ori_logs[i] = strs
      i += 1
      file = File.new(params[:path])
      strs = file.readlines[i]
    end
    index = 0
    if params[:time] != nil
      head = 0
      tail = ori_logs.size
      middle = (head + tail) / 2
      index = binary_search({:head => head, :middle => middle, :tail => tail, :array => ori_logs, :time => params[:time]})
    end
    logs = []
    i = 0
    while index < ori_logs.size
      
      time_value = ori_logs[index].slice(6, 15)
      time_value = time_value.delete("-") 
      
      cpu_value = get_value({:log => ori_logs[index], :key => "CPU", :offset => 5})
      memTotal_value = get_value({:log => ori_logs[index], :key => "memTotal", :offset => 10})
      memFree_value = get_value({:log => ori_logs[index], :key => "memFree", :offset => 9})             
      rece_value = get_value({:log => ori_logs[index], :key => "Rece", :offset => 6})      
      tran_value = get_value({:log => ori_logs[index], :key => "Tran", :offset => 6})
      
      logs[i] = { :Time => time_value, :CPU => cpu_value, :memTotal => memTotal_value, :memFree => memFree_value, :Rece => rece_value, :Tran => tran_value}
      
      i +=1
      index += 1
    end
    logs
  end
	
  def Log.binary_search params
    time = params[:time]
    time.insert(8, "-")
    head = params[:head]
    tail = params[:tail]
    middle = params[:middle]
    array = params[:array]
    while(head < tail - 1)
      temp_time = array[middle].slice(6, 15)
      if temp_time < time
        head = middle
      else
        tail = middle
      end
      middle = (head + tail) / 2
    end
    if array[head].slice(6, 15) == time
      return head
    else
      return tail
    end
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