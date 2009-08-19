require File.dirname(__FILE__) + '/server_settings'

class NetworkInterfaceConfigFile
	TemplateFile = File.dirname(__FILE__) + "/template/interfaces.template"

	def initialize(dev)
		@dev = dev
		@content = []
		@multiple_count = 0
		begin
			File.open(TemplateFile, 'r') do |file|
				@content = file.readlines
			end
		rescue
			puts "Cannot read template file #{TemplateFile}"
		end
		@content << ""
	end


	def add(ip_addr, net_mask)
		if_name = "#{@dev}:#{@multiple_count.to_s}"
		@content << "auto #{if_name}"
		@content << "iface #{if_name} inet static"
		@content << "address #{ip_addr}"
		@content << "netmask #{net_mask}"
		@content << ""

		@multiple_count += 1
	end

	def write_to_system
		ServerSettings.write(@content, Server_Network_Interface_File_Path)
	end
end

