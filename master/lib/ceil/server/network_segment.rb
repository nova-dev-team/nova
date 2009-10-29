CEIL_ROOT = File.dirname(__FILE__) + "/.."

require "#{CEIL_ROOT}/common/ip"
require "#{CEIL_ROOT}/server/dhcp_conf"
require "#{CEIL_ROOT}/server/if_conf"

class NetworkSegment
  def initialize(dev, net_mask)
    @dev = dev
    @global_net_mask = net_mask
  end

  def reconstruct(segment_begin, req, dev, global_net_mask)
    segment_begin = IpV4Address.calc_prev(segment_begin)
    nic = NetworkInterfaceConfigFile.new(@dev)
    global_net_id = 0
    global_net_mask = @global_net_mask
    global_net_segment = IpV4Address.calc_network(segment_begin, global_net_mask)		
    dhcp = DHCPConfigFile.new(global_net_segment, global_net_mask)


    req.sort
    req.each do |seg_len, seg_num| 
      puts "seglen=#{seg_len}"
      puts "segnum=#{seg_num}"

      seg_net_mask = IpV4Address.calc_net_mask(seg_len)				
      seg_num.times {
        segment_machine_id = 0
        segment_begin = IpV4Address.calc_next_segment(segment_begin, seg_len)

        puts "allocate #{segment_begin}/#{seg_net_mask}, length = #{seg_len}"

        segment_begin = IpV4Address.calc_next(segment_begin)
        nic.add(IpV4Address.calc_gateway(segment_begin, seg_net_mask), seg_net_mask)

        seg_len.times {
          segment_begin = IpV4Address.calc_next(segment_begin)
          dhcp.add("nova-#{global_net_id}-#{segment_machine_id}", 
             IpV4Address.generate_mac(segment_begin), 
             segment_begin,
             seg_net_mask)
          segment_machine_id += 1
        }

        segment_begin = IpV4Address.calc_next(segment_begin)
      }
      global_net_id += 1

    end
    nic.write_to_system
    dhcp.write_to_system
  end

  def allocate(segment_length)

  end

  def free(segment_name)

  end
end


#ns = NetworkSegment.new("eth1", "255.255.0.0")
#ha = {4 => 7, 5 => 2, 12 => 2, 40 => 1}
#ns.reconstruct("10.0.10.0", ha)

