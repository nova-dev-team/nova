require File.dirname(__FILE__) + '/package_downloader'
require File.dirname(__FILE__) + '/log_report'
require File.dirname(__FILE__) + '/config'
require File.dirname(__FILE__) + '/venv'

class Ceil
  def initialize
    @log = nil
    @env = nil
    @cc = nil
    @install_list = nil
    @package_downloader = nil
    @key_downlaoder = nil
  end

  def check_iso
    @env = Environment.new
    `reboot` if ! @env.check_iso

    begin
      @cc = ClusterConfiguration.new(@env.local_addr)

      #server_addr = "192.168.0.110"
      server_addr = @env.default_gateway
      local_addr = @env.local_addr

      if ! @cc.fetch_by_iso
        puts "Cannot find config files in cdrom"
        return nil
      end

      ubuntu_filename_hostname = '/etc/hostname'
      begin
        File.open(ubuntu_filename_hostname, 'wb') do |file|
          file.puts @cc.host_name
        end
        @env.reconfig_network('eth0')
      rescue
      end

      #@cc.fetch_by_http("192.168.0.110")
      @log = LogReporter.new(server_addr, local_addr)
      @log.send("0", "ceil", "configuration fetched")

      @package_downloader = PackageDownloader.new(@cc.package_server, @cc.package_server_port, @cc.package_server_username, @cc.package_server_password)
      @key_downloader = PackageDownloader.new(@cc.key_server, @cc.key_server_port, @cc.key_server_username, @cc.key_server_password)

      @install_list = @cc.inst_list.split
    rescue => e
      puts "Cannot fetch configuration info from server #{@env.server_addr}, #{e.to_s}"
      return nil
    end
  end

  def check
    @env = Environment.new
    `reboot` if ! @env.check

    begin
      @cc = ClusterConfiguration.new(@env.local_addr)

      #server_addr = "192.168.0.110"
      server_addr = @env.default_gateway

      retr = false
      while ! retr
        begin
          retr = @cc.fetch_by_http(server_addr)
        rescue => e
          retr = false
          puts "ERROR #{e.to_s}"
        rescue Timeout::Error => e
          retr = false
          puts "ERROR #{e.to_s}"
        end
        puts "NOW SLEEP 10s for Safety"
        sleep 10 							
      end

  			 #puts "Cannot connect to server through http"
  			 #return nil

      #@cc.fetch_by_http("192.168.0.110")
      			@log = LogReporter.new(server_addr, @env.local_addr)
      @log.send("0", "ceil", "configuration fetched")
      @package_downloader = PackageDownloader.new(@cc.package_server)

      @key_downloader = PackageDownloader.new(@cc.key_server)

      @install_list = @cc.inst_list.split
    rescue => e
      puts "Cannot fetch configuration info from server #{@env.server_addr}, #{e.to_s}"
      return nil
    end
  end

  def start
    @install_list.each do |app_name|
      begin
        puts "Processing #{app_name}"
        temp_path = nil

  	    @log.send("10", "#{app_name}", "server using #{@cc.package_server_type}")
        paths = []

        @cc.character.each do |role|
          case @cc.package_server_type
            when "nfs"
      				temp_path = @package_downloader.package_by_nfs(app_name, role)
            when "ftp"
              temp_path = @package_downloader.package_by_ftp(app_name, role)
            else
              @log.send("-10", "#{app_name}", "package server type #{@cc.package_server_type} not supported.")
              puts "package server type #{@cc.package_server_type} not supported."
              break
          end
          @cc.generate_config_file(temp_path)
          paths << temp_path
        end
        @log.send("15", "#{app_name}")

        #@cc.generate_config_file(temp_path)

        @log.send("30", "#{app_name}")
        paths.each do |temp_path|
          if File.exists?(temp_path + "/entry.sh")
            system "/bin/sh #{File.dirname(__FILE__)}/exec.sh #{temp_path} entry.sh #{@cc.host_name} #{@cc.cluster_name}"
            @log.send("35", "#{app_name}")
          else
            @log.send("-30", "#{app_name}", "entry of package #{app_name} not found, maybe #{@cc.package_server_type} downloading failed, or the package is broken")
            puts "entry of package #{app_name} not found, maybe #{@cc.package_server_type} downloading failed, or the package is broken"
            break
          end
        end
  	    @log.send("50", "#{app_name}", "using #{@cc.key_server_type}")

        case @cc.key_server_type
          when "nfs"
    				temp_path = @key_downloader.key_by_nfs(@cc.cluster_name, app_name)
          when "ftp"
            temp_path = @key_downloader.key_by_ftp(@cc.cluster_name, app_name)
          else
            @log.send("-50", "#{app_name}", "key server type #{@cc.key_server_type} not supported.")
            puts "key server type #{@cc.key_server_type} not supported."
            next
        end

  			@log.send("55", "#{app_name}")

        @cc.generate_config_file(temp_path)

        @log.send("70", "#{app_name}")

        if File.exists?(temp_path + "/attach.sh")
  				system "/bin/sh #{File.dirname(__FILE__)}/exec.sh #{temp_path} attach.sh #{@cc.host_name} #{@cc.cluster_name}"
  				
  				@log.send("75", "#{app_name}")
  			else
  			  @log.send("-70", "#{app_name}", "key of package #{app_name} not found, maybe #{@cc.key_server_type} downloading failed, or the key is broken")
  			  puts "key of package #{app_name} not found, maybe #{@cc.key_server_type} downloading failed, or the key is broken"
  			  next
  			end
        #use system instead puts..
        @log.send("80", "#{app_name}")
      rescue => e
        @log.send("-80", "#{app_name}", "Client Error:#{e.to_s}")
        puts "error on installing app #{app_name}:"
        puts "#{e.to_s}"
        puts "#{$!}"		
      end
    end
    @log.send("100", "")
  end	
end

ceil = Ceil.new
ceil.start if ceil.check_iso
puts "ceil finished"


