

class UDaemon

  SLEEP = 10
  DELAY_START = 10
  @@t = -1

  def initilize
    @shutdown = false
  end

  def engine
    until @shutdown
      begin
        puts "UDaemon scanning ..."
        UpdateImageQueue.establish_connection DB_CONF
        UpdateImageQueue.find(:all,
                              :conditions => { :progress => -1},
                              :lock => true).each do |req|
          puts "UDaemon: find req for #{ req.url}, #{ req.priority }"
          # todo: sort priority
          fn = U0.download req
          req.destroy
          S0.add_sys_img fn
        end
      rescue => e
        puts "U Engine Capture Error in Thread. #{e}"
        # puts "U Engine Capture Error in Thread. #{e.backtrace}"
      end
      puts "UDaemon sleep (#{ SLEEP })..."
      sleep SLEEP
    end
    puts 'Stop Update Daemon'
  end

  def restart
    puts 'Starting Update Daemon'
    if @@t > 0
        puts 'The Update Engins has been running.'
        # do nothing
        return true
    end

    @@t = Process.fork do
      Signal.trap("USR1") do
        puts "Terminating..."
        @shutdown = true
      end

      sleep DELAY_START
      engine
    end
    Process.detach(@@t)
    return true
  end

  def stop
    puts 'Update Engine Stopping' + "(#{ @@t})"
    Process.kill('USR1', @@t)
    Thread.new {
      Process.wait(@@t)
      @@t = -1
      puts 'Storage Engine Stopped'
    }
  end

  def status
    return @@t > 0
  end
end

