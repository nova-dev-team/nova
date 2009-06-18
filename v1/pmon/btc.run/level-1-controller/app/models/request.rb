# == Schema Information
# Schema version: 20090403015007
#
# Table name: requests
#
#  id         :integer         not null, primary key
#  kind       :string(255)     not null
#  uuid       :string(255)     not null
#  created_at :datetime
#  updated_at :datetime
#

class Request < ActiveRecord::Base
end
