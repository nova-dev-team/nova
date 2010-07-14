class LogsController < ApplicationController
	
  LogFilePath = "#{RAILS_ROOT}/log/pm_top.log"
  
  def show
    if File.exists?(LogFilePath)
      logs = []
      if(params[:time] != nil) 
        logs = Log.get_tail({:path => LogFilePath, :time => params[:time]})
      else
        logs = Log.get_tail({:path => LogFilePath })
      end
      reply_success "Get Log Successful!", :data => logs
    else
      reply_failure "Log File not exist!"
    end     
  end
	
end