# Model for virtual clusters.
#
# Author::    Santa Zhang
# Since::     0.3

require "uuidtools"
require 'utils'

class Vcluster < ActiveRecord::Base

  belongs_to :user
  has_many :vmachines

  # Try to allocate a cluster.
  # Returns {success, message} pair.
  #
  # Since::   0.3
  def Vcluster.alloc_cluster name, size
    if Vcluster.find_by_cluster_name name
      return {:success => false, :message => "There is already a cluster named '#{name}'!"}
    end

    # make sure 'size' is integer
    size = size.to_i

    # determine 'first_ip'
    first_ip = nil

    # (start_ip_ival, end_ip_ival), end_ip_ival is inclusive
    used_ip_segments = []

    # gateway takes 1 ip
    gateway_ip_ival = IpTools.ipv4_to_i Setting.vm_gateway
    used_ip_segments << [gateway_ip_ival, gateway_ip_ival]
    Vcluster.all.each do |vcluster|
      used_first_ip = IpTools.ipv4_to_i vcluster.first_ip
      used_ip_segments << [used_first_ip, used_first_ip + vcluster.cluster_size - 1]
    end
    used_ip_segments.sort! {|a, b| a[0] <=> b[0]} # sort the segments

    first_usable_ip_ival = IpTools.ipv4_to_i Setting.vm_first_ip
    last_usable_ip_ival = IpTools.ipv4_to_i(IpTools.last_ip_in_subnet Setting.vm_first_ip, Setting.vm_subnet_mask)

    test_ip_ival = first_usable_ip_ival
    while test_ip_ival + size - 1 < last_usable_ip_ival do
      test_segment = [test_ip_ival, test_ip_ival + size - 1]
      usable = true # whether this test segment is usable
      used_ip_segments.each do |used_seg|
        unless test_segment[1] < used_seg[0] or test_segment[0] > used_seg[1]
          # the test segment collides with some used segment
          usable = false
          break
        end
      end
      if usable
        # found a usable segment
        break
      else
        used_ip_segments.each do |used_seg|
          if used_seg[1] + 1 > test_ip_ival
            test_ip_ival = used_seg[1] + 1
            break
          end
        end
      end
    end

    unless test_ip_ival + size - 1 < last_usable_ip_ival
      return {:success => false, :message => "There is not enough IP available for VMs!"}
    end

    vc = Vcluster.new
    vc.first_ip = IpTools.i_to_ipv4 test_ip_ival
    vc.cluster_size = size
    vc.cluster_name = name
    vc.save

    return {:success => true, :message => "Successfully created cluster '#{name}' with size of #{size}!"}
  end

end

