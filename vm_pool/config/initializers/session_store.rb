# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_vm_pool_session',
  :secret      => '87b91ebc40c3d6cb3392db2bbfea0dd1ba6793c0d51913324e86ddbfee43ef2f1ff656294ef704d3a25d1c4ed7b45fe02e57f74a0b03fee422218028e0d58dcc'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
