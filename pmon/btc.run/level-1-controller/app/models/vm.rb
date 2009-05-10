# == Schema Information
# Schema version: 20090403015007
#
# Table name: vms
#
#  id         :integer         not null, primary key
#  vm_uuid    :string(255)     not null
#  vm_status  :string(255)     not null
#  vm_def     :text
#  created_at :datetime
#  updated_at :datetime
#

class Vm < ActiveRecord::Base
end
