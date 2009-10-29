require File.dirname(__FILE__) + "/../common/ip"

class Environment
	attr_reader :local_addr, :local_mask, :server_addr;

 	def initialize
		@local_addr = nil
		@local_mask = nil;
		@auto_server_ip = nil;
	end

	def check
		local_addr = `ifconfig eth0 | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;
		local_mask = `ifconfig eth0 | grep Mask | awk \'{print $4}' | awk -F: '{print $2}\'`;

		local_addr = local_addr.chomp
		local_mask = local_mask.chomp

		if IpV4Address::ip?(local_addr) && IpV4Address::ip?(local_addr)
			@local_addr = local_addr;
			@local_mask = local_mask;
			@server_addr = IpV4Address::calc_gateway(local_addr, local_mask);
		else
			puts "cannot fetch correct information for eth0!"
			return nil
		end
	end
end

#env = Environment.new;
#env.check;
#puts env.local_ip;
#puts env.local_mask;
#puts env.server_ip;


