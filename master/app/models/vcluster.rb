# Model for virtual clusters.
#
# Author::    Santa Zhang
# Since::     0.3

require "uuidtools"
require 'utils'

class Vcluster < ActiveRecord::Base

  include IpTools

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

    vc = Vcluster.new

    # TODO determine 'first_ip'
    vc.first_ip = "TODO"
    vc.cluster_size = size
    vc.cluster_name = name
    vc.save

    return {:success => true, :message => "Successfully created cluster '#{name}' with size of #{size}!"}
  end

end

