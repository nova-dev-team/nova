# this is a link between group & user model

class Ugrelationship < ActiveRecord::Base

  belongs_to :group
  belongs_to :user

end
