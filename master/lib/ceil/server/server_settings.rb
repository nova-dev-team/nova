require File.dirname(__FILE__) + '/server_nfs_settings'
#require File.dirname(__FILE__) + "/server_carrier_settings"

SERVER_DHCPD_CONFIG_FILE_PATH = "/etc/dhcp3/dhcpd.conf"
#Server_Dhcpd_Config_File_Path = "/etc/dhcp3/dhcpd.conf"
SERVER_DHCPD_INTERFACE_FILE_PATH = "/etc/default/dhcp3-server"

#Server_Dhcpd_Interface_File_Path = "/etc/default/dhcp3-server"
SERVER_NETWORK_INTERFACE_FILE_PATH = "/etc/network/interfaces"
#Server_Network_Interface_File_Path = "/etc/network/interfaces"
#FUCKING CONST

module ServerSettings
	def ServerSettings.write(content, config_file)
		content << ""
		content << "# #{`date`}"
		content << ""
		note = `date "+%Y%m%d%H%M"`.chomp
		#system "mv #{config_file} #{config_file}.backup.#{note}"
		
		config_file = "/tmp/" + File.basename(config_file)

		begin
			File.open(config_file, 'w') do |file|
				content.each do |line|
					file.puts(line)
					puts(line)
				end
			end
		rescue
			puts "Cannot write content to system config file #{config_file}!"
		end
	end
end

