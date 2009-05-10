

class SDaemon

  MAX_COPY_NUM = 3
  SLEEP = 10
  DELAY_START = 10
  @@t = -1

  def initilize
    @shutdown = false
  end

  def cache_engine

    until @shutdown
      begin
        puts "SDaemon scanning ..."
        Vdisk.establish_connection DB_CONF
        Vdisk.find_all_by_vd_kind_and_vd_status('system', 'ready').each do |svd|
          puts ">>>>1.3 #{svd}"
#          (MAX_COPY_NUM - Vdisk.count(:conditions => { :vd_template => svd.vd_uuid, :vd_status => 'ready' })).times do |n|
          (MAX_COPY_NUM - Vdisk.count(:conditions => { :vd_template => svd.vd_uuid})).times do |n|
            puts "#{n} => #{svd} => #{svd.filename}"
            S0.clone svd
          end
        end
        Vdisk.find_all_by_vd_status('delete').each do |dvd|
          puts ">>>>1.4 #{dvd}"
          puts "#{ dvd.vd_uuid } will be deleted"
          S0.delete dvd
        end
      rescue => e
        puts "S Engine Capture Error in Thread. #{e}"
        # puts "S Engine Capture Error in Thread. #{e.backtrace}"
      end
      puts "SDaemon sleep (#{ SLEEP })..."
      sleep SLEEP
    end
    puts 'Stop Storage Daemon'
  end

  def restart
    puts 'Starting Storage Daemon'
    if @@t > 0
        puts 'The Storage Engins has been running.'
        # do nothing
        return true
    end

    @@t = Process.fork do
      Signal.trap("USR1") do
        puts "Terminating..."
        @shutdown = true
      end

      sleep DELAY_START
      cache_engine
    end
    Process.detach(@@t)
    return true
  end

  def stop
    puts 'Storage Engine Stopping' + "(#{ @@t})"
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

