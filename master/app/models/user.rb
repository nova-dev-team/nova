require 'digest/sha1'

class User < ActiveRecord::Base
  include Authentication
  include Authentication::ByPassword
  include Authentication::ByCookieToken

  validates_presence_of     :login
  validates_length_of       :login,    :within => 3..20
  validates_uniqueness_of   :login

  ## NOTE original :with => Authentication.login_regex was modified.
  ## NOTE Users SHOULD not register with names starting with '.' or '_', which is reserved by system
  ## NOTE also, UPPERCASE is not allowed for normal users
  validates_format_of       :login,    :with => /\A[a-zA-Z_\.]+[a-zA-Z0-9_\.]*\z/, :message => Authentication.bad_login_message

  validates_format_of       :name,     :with => Authentication.name_regex,  :message => Authentication.bad_name_message, :allow_nil => true
  validates_length_of       :name,     :maximum => 40

  validates_presence_of     :email
  validates_length_of       :email,    :within => 6..40 #r@a.wk
  validates_uniqueness_of   :email
  validates_format_of       :email,    :with => Authentication.email_regex, :message => Authentication.bad_email_message

  

  # HACK HACK HACK -- how to do attr_accessible from here?
  # prevents a user from submitting a crafted form that bypasses activation
  # anything else you want your user to change should be added here.
  attr_accessible :login, :email, :name, :password, :password_confirmation

  has_many :ugrelationships
  has_many :groups, :through => :ugrelationships
  has_many :vclusters

  # Authenticates a user by their login name and unencrypted password.  Returns the user or nil.
  #
  # uff.  this is really an authorization, not authentication routine.  
  # We really need a Dispatch Chain here or something.
  # This will also let us return a human error message.
  #
  def self.authenticate(login, password)
    return nil if login.blank? || password.blank?
    u = find_by_login(login.downcase) # need to get the salt
    u && u.authenticated?(password) ? u : nil
  end

  def login=(value)
    write_attribute :login, (value ? value.downcase : nil)
  end

  def email=(value)
    write_attribute :email, (value ? value.downcase : nil)
  end

  def in_group? group_name
    groups.include?(Group.find_by_name group_name)
  end
  
  def is_root?
    login == "root"
  end
  
  def is_admin?
    groups.include?(Group.find_by_name "admin")
  end

  protected
    


end
