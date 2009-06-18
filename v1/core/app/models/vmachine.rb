class Vmachine < ActiveRecord::Base

  belongs_to :pmachine
  belongs_to :vcluster
  belongs_to :vimage

end
