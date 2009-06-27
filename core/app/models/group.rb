class Group < ActiveRecord::Base

  validates_presence_of   :name
  validates_length_of     :name, :within => 3..40, :message => "Group name should have length 3..40"
  validates_uniqueness_of :name
  validates_format_of     :name, :with => /\A\w[\w\-_]*\z/, :message => "Only digits, alpha, '-' and '_' allowed"
                          # only alpha, digits & '-', '_' allowed

  has_many :ugrelationships
  has_many :users, :through => :ugrelationships

  def Group.list_without_applying
    groupList = []
    Group.all.each do |g| groupList << {
        :name => g.name,
        :id => g.id
      } if g.name != 'applying'
    end
    return groupList
  end

end
