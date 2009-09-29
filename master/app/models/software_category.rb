class SoftwareCategory < ActiveRecord::Base
	has_many :softwares
  has_many :vsrelationships
  has_many :vdisks, :through => :vsrelationships
end
