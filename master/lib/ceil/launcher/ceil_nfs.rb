require File.dirname(__FILE__) + '/package_downloader'
require File.dirname(__FILE__) + '/config'
require File.dirname(__FILE__) + '/venv'

class Ceil
	def initialize
		@env = Environment.new
		@env.check
		@cc = ClusterConfiguration.new(@env.local_addr)
		@install_list = nil
		@downloader = nil
	end

	def check
		begin
			#@cc.fetch_by_nfs(@env.server_addr)
			@cc.fetch_by_http("192.168.0.110")
			@downloader = PackageDownloader.new("192.168.0.110")
			@install_list = @cc.inst_list.split
		rescue
			puts "Cannot fetch configuration info from server #{@env.server_addr}"
			nil
		end
	end

	def start
		@install_list.each do |app_name|
			begin
				puts "processing #{app_name}"
				temp_path = @downloader.package_by_nfs(app_name)
				@cc.generate_config_file(temp_path)
				system "#{File.dirname(__FILE__)}/exec.sh #{temp_path} entry.sh #{@cc.host_name}"

				temp_path = @downloader.key_by_nfs(@cc.cluster_name, app_name)
				@cc.generate_config_file(temp_path)
				system "#{File.dirname(__FILE__)}/exec.sh #{temp_path} attach.sh #{@cc.host_name}"
			rescue
				puts "error on installing app #{app_name}"
				puts "#{$!}"		
			end
		end
	end	

end

#ceil = Ceil.new
#ceil.start if ceil.check

