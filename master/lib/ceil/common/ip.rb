
module IpV4Address
	def IpV4Address.ip?(ip)
		ip =~ /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
	end

	def IpV4Address.generate_mac(ip_addr, mac54 = '54:7E')
		mac54 + ':' + ip_addr.split('.').inject do |p, x|
			p += ":" + ("%02x" % x.to_i)
		end
	end


	def IpV4Address.calc_segment_bits(count)
		return (Math.log(count + 2) / Math.log(2)).ceil
	end

	def IpV4Address.calc_segment_length(count)
		return 2 ** calc_segment_bits(count)
	end

	def IpV4Address.string_to_integer(ip_addr)
		res = 0
		ip_addr.split(".").each do |seg|
			res = (res << 8) + seg.to_i
		end
		return res
	end
	def IpV4Address.integer_to_string(ip_addr)
		res = []
		4.times {
			res = res << (ip_addr & 255)
			ip_addr = ip_addr >> 8
		}
		return res.reverse.join(".")
	end

	def IpV4Address.calc_net_mask(count)
		count = calc_segment_length(count)
		mask = 1
		net_mask = 0
		32.times {
			bit = count.zero? ? 1 : (1 & count)
			net_mask += bit * mask
			count = count >> 1
			mask = mask << 1
		}
		res = []
		4.times {
			res << (net_mask & 255)
			net_mask = net_mask >> 8
		}
		return res.reverse.join(".")
	end

	def IpV4Address.string_to_binary(ip_addr)
		res = string_to_integer(ip_addr)
		return res.to_s(2).rjust(32, "0")
	end

	def IpV4Address.calc_gateway(ip_addr, net_mask)
		return integer_to_string(((string_to_integer(ip_addr) & string_to_integer(net_mask)) + 1))
	end
	def IpV4Address.calc_network(ip_addr, net_mask)
		return integer_to_string((string_to_integer(ip_addr) & string_to_integer(net_mask)))
	end

	def IpV4Address.calc_next_segment(current_addr, segment_length)
		count = calc_segment_bits(segment_length)
		addr = string_to_integer(current_addr)
		addr = ((addr >> count) + 1) << count
		return integer_to_string(addr) #, integer_to_string(addr + 2 ** count)
	end
	def IpV4Address.calc_next(current_addr)
		return integer_to_string(string_to_integer(current_addr) + 1)
	end
	def IpV4Address.calc_prev(current_addr)
		return integer_to_string(string_to_integer(current_addr) - 1)
	end
end


class String
	def to_bin
		#puts "SELF = #{self}"
		IpV4Address.string_to_binary(self.chomp)
	end		
end


#puts IpV4Address.calc_segment_length(8)
#puts IpV4Address.calc_segment_length(18)
#puts IpV4Address.calc_segment_length(388)
#puts IpV4Address.calc_segment_length(40)


#puts IpV4Address.calc_net_mask(8).to_bin
#puts IpV4Address.calc_net_mask(18)
#nm = IpV4Address.calc_net_mask(338)
#puts IpV4Address.string_to_integer(nm)
#puts nm.to_bin
#puts "10.0.0.33".to_bin


#puts IpV4Address.calc_gateway("10.0.0.33", "255.255.255.240")
#puts IpV4Address.calc_gateway("10.0.0.34", "255.255.255.240")

#puts IpV4Address.string_to_integer("10.0.0.33")
#puts IpV4Address.string_to_integer("10.0.0.34")
#puts IpV4Address.string_to_integer("255.255.255.240")

#puts IpV4Address.calc_next_segment("10.0.0.33", 338)


#puts IpV4Address.calc_net_mask(40)


