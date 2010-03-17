#! /usr/bin/ruby

IPREGEXP=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/


def create_bridge ifname, brname

#    ifname = 'eth0'
#    brname = 'br0'



    local_addr = `ifconfig #{ifname} | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;
    local_mask = `ifconfig #{ifname} | grep Mask | awk \'{print $4}' | awk -F: '{print $2}\'`;
    local_gateway = `route -n | grep #{ifname} | awk '$1 ~ /default|0.0.0.0/ { print $2 }'`
    local_addr = local_addr.chomp
    local_mask = local_mask.chomp
    local_gateway = local_gateway.chomp

    puts "IP Addr = #{local_addr}"
    puts "Netmask = #{local_mask}"
    puts "Default Gateway = #{local_gateway}"

    if ((local_addr =~ IPREGEXP) &&
	(local_mask =~ IPREGEXP) &&
	(local_gateway =~ IPREGEXP))
	    `ifconfig #{brname} down`
	    `brctl delbr #{brname}`
	    `brctl addbr #{brname}`
	    `brctl setbridgeprio #{brname} 0`

	    `brctl addif #{brname} #{ifname}`
	    `ifconfig #{ifname} 0.0.0.0`
	    `ifconfig #{brname} #{local_addr} netmask #{local_mask}`
	    `brctl sethello #{brname} 1`
	    `brctl setmaxage #{brname} 4`
	    `brctl setfd #{brname} 4`
	    `ifconfig #{brname} up`
	    `route add default gw #{local_gateway}`
    else
      puts 'information about eth0 is not available!'
    end

end

#puts local_addr
#puts local_mask

