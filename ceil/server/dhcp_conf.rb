require File.dirname(__FILE__) + '/server_settings'
require File.dirname(__FILE__) + '/../common/ip'

class DHCPConfigFile
	TemplateFile = File.dirname(__FILE__) + "/template/dhcpd.conf.template"
	
	def initialize(sub_net, net_mask)
		@content = []
		begin
			File.open(TemplateFile, 'r') do |file|
				@content = file.readlines
			end
		rescue
			puts "Cannot read template file #{TemplateFile}!"
		end
		@content << "subnet #{sub_net} netmask #{net_mask} \{"
		@content << "\}"
		@content << ""
		
	end
	
	def add(host_name, mac_addr, ip_addr, net_mask)
		@content << "host #{host_name} \{"
		@content << "	hardware ethernet #{mac_addr};"
		@content << "	fixed-address #{ip_addr};"
		@content << "	option subnet-mask #{net_mask};"
		@content << "	option routers #{IpV4Address.calc_gateway(ip_addr, net_mask)};"
		@content << "\}"
		@content << ""
	end
 
	def write_to_system
		ServerSettings.write(@content, Server_Dhcpd_Config_File_Path)
	end
	
	def set_dhcp_interface(dev)		
		File.open(Server_Dhcpd_Interface_File_Path, 'w') do |file|
			file.puts("INTERFACES=\"#{dev}\"")
		end
	end
end

