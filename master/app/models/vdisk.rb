class Vdisk < ActiveRecord::Base
  #has_many :ugrelationships
  #has_many :groups, :through => :ugrelationships
  has_many :vsrelationships
  has_many :software_categories, :through => :vsrelationships
end
