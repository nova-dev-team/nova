class Vmachine < ActiveRecord::Base

  belongs_to :pmachine  # could be nil
  belongs_to :vcluster  # could be nil
  belongs_to :user  # cannot be nil

end
