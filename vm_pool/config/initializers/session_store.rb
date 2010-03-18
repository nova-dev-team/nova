# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_vm_pool_session',
  :secret      => 'adc4529891e6614b0675237ef0eefe6c1e505446b754d10add4180a578ed29748a90245f18f499ce80c1c138fb2fe8ed3380b2a36855b539e7293387870fe0b3'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
