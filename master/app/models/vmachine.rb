# The model for virtual machines.
#
# Author::    Santa Zhang
# Since::     0.3

class Vmachine < ActiveRecord::Base

  belongs_to :vcluster
  belongs_to :pmachine
  has_many :vmachine_infos

end
