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

    # (start_ip_ival, size)
    used_ip_segments = []

    # gateway takes 1 ip
    used_ip_segments << [(IpTools.ipv4_to_i Setting.vm_gateway), 1]
    Vcluster.all.each do |vcluster|
      used_ip_segments << [(IpTools.ipv4_to_i vcluster.first_ip), vcluster.cluster_size]
    end

    vc = Vcluster.new
    vc.first_ip = first_ip
    vc.cluster_size = size
    vc.cluster_name = name
    vc.save

    return {:success => true, :message => "Successfully created cluster '#{name}' with size of #{size}! #{used_ip_segments}"}
  end

end

