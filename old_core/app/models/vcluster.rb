class Vcluster < ActiveRecord::Base

  belongs_to :user
  has_many :vmachines

end
