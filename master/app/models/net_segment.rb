CEIL_ROOT = "#{RAILS_ROOT}/lib/ceil/"

require "#{CEIL_ROOT}/common/ip"
require "#{CEIL_ROOT}/server/dhcp_conf"
require "#{CEIL_ROOT}/server/if_conf"

class NetSegment < ActiveRecord::Base
  belongs_to :vcluster

  #protected
  #public
  
  #example
  #segment_begin = "10.0.10.0"
  #global_net_mask = "255.255.0.0"
  #req = { 4=>1, 7=>5, 200=>3 }
  #dev = "eth1"
  def NetSegment._reconstruct(segment_begin, global_net_mask, req, dev)
    NetSegment.delete_all
    
		segment_begin = IpV4Address.calc_prev(segment_begin)
		nic = NetworkInterfaceConfigFile.new(dev)
		global_net_id = 0
		global_net_segment = IpV4Address.calc_network(segment_begin, global_net_mask)		
		dhcp = DHCPConfigFile.new(global_net_segment, global_net_mask)

		req.sort.each do |seg_len, seg_num| 
			puts "seglen=#{seg_len}"
			puts "segnum=#{seg_num}"

			seg_net_mask = IpV4Address.calc_net_mask(seg_len)				
			
			seg_num.times {
				segment_machine_id = 0
				segment_begin = IpV4Address.calc_next_segment(segment_begin, seg_len)
				
				puts "allocate #{segment_begin}/#{seg_net_mask}, length = #{seg_len}"
        segment_begin = IpV4Address.calc_next(segment_begin)
				
				nic.add(IpV4Address.calc_gateway(segment_begin, seg_net_mask), seg_net_mask)
				
				segment_name = "nova-#{global_net_id}"
  			global_net_id += 1
				
				net = NetSegment.new(:name    => segment_name, 
				                     :head_ip => segment_begin,
				                     :size    => seg_len,
				                     :mask    => seg_net_mask)
		    if !net.save
		      puts "FUCKED!!"
		    end
		    
				seg_len.times {
				  
  				segment_begin = IpV4Address.calc_next(segment_begin)
					
	  			dhcp.add("#{segment_name}-#{segment_machine_id}", 
        					 IpV4Address.generate_mac(segment_begin), 
	  		           segment_begin,
  						     seg_net_mask)
  				segment_machine_id += 1
				}

				segment_begin = IpV4Address.calc_next(segment_begin)
			}

		end
		nic.write_to_system
		dhcp.write_to_system
	end 
  
  def _list
    list = []
    ip = self.head_ip
    self.size.times do |n|
      list << [self.name + "-#{n}", ip, IpV4Address.generate_mac(ip) ]
      ip = IpV4Address.calc_next(ip)
    end
    return list
  end

  public

  def NetSegment.alloc(size, vcluster_id)
    find(:all, :conditions => { :used => false}).each do |net|
      if net.size >= size
        net.vcluster = Vcluster.find(vcluster_id)
        net.used = true
        net.save! rescue next
        return net._list
      end
    end
  end

=begin
  def test
    IpV4Address.string_to_binary("10.0.0.5")
  end
=end
  
  def free
    #self.vcluster.net_segment = nil
    self.vcluster = nil
    self.used = false
    self.save!
  end
end

