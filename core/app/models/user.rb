class User < ActiveRecord::Base

  has_many :groups
  has_many :vclusters
  has_many :vmachines

end
