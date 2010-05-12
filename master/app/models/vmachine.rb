# The model for virtual machines.
#
# Author::    Santa Zhang
# Since::     0.3

class Vmachine < ActiveRecord::Base

  belongs_to :vcluster
  belongs_to :pmachine
  has_many :vmachine_infos

  def hostname
    vc = self.vcluster
    vc_name_length = vc.cluster_name.length
    self.name[(vc_name_length + 1)..-1]
  end

end
