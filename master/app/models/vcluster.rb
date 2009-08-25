class Vcluster < ActiveRecord::Base
  has_one :net_segment
  has_many :vmachines
end
