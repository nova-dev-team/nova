class Vsrelationship < ActiveRecord::Base
  belongs_to :vdisk
  belongs_to :software_category
end
