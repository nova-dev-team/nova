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
	
	def check_win
		@env = Environment.new
    while ! @env.check_win
      puts "Waiting for unconfigured networking... 10sec"
      sleep 10
    end

		begin
      #@env.local_addr = "10.0.40.18"
      #@env.server_addr = "166.111.131.32"
      
		  @cc = ClusterConfiguration.new(@env.local_addr)
      #@cc = ClusterConfiguration.new("10.0.40.18")

      server_addr = @env.server_addr
      
			#server_addr = "166.111.131.32"
      
      puts "Server Address should be #{server_addr}"
			
			while !@cc.fetch_by_http(server_addr)
        puts "Waiting for server response... 10sec"
        sleep 10
  		end
			
      @log = LogReporter.new(server_addr, @env.local_addr)
			@log.send("0", "ceil", "configuration fetched")
			  
			@package_downloader = PackageDownloader.new(@cc.package_server)
			#@package_downloader = PackageDownloader.new(server_addr)
			@key_downloader = PackageDownloader.new(@cc.key_server)
			@install_list = @cc.inst_list.split
		rescue => e
			puts "Cannot fetch configuration info from server #{@env.server_addr}, #{e.to_s}"
			return nil
		end
	end
	
	def start_win
		@install_list.each do |app_name|
			begin
				puts "Processing #{app_name}"
				
				system "d:\\bscript\\#{app_name}.bat"
				
				temp_path = nil
  	    @log.send("10", "#{app_name}", "server using #{@cc.package_server_type}")
				paths = []

        case @cc.package_server_type
          #when "nfs"
            #temp_path = @package_downloader.package_by_nfs(app_name, role)
          when "ftp"
            temp_path = @package_downloader.win_shortcut_by_ftp(app_name)
          else 
            @log.send("-10", "#{app_name}", "package server type #{@cc.package_server_type} not supported.")
            puts "package server type #{@cc.package_server_type} not supported."
            break
        end

        @log.send("35", "#{app_name}")
				
  			@log.send("55", "#{app_name}")
				
				@log.send("70", "#{app_name}")
				#use system instead puts..
				@log.send("80", "#{app_name}")
			rescue => e
			  @log.send("-80", "#{app_name}", "Client Error:#{e.to_s}")
				puts "error on installing #{app_name}:"
				puts "#{e.to_s}"
				puts "#{$!}"		
			end
		end
		@log.send("100", "ceil")
	end	
end

ceil = Ceil.new
ceil.start_win if ceil.check_win

