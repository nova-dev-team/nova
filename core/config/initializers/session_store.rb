# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_core_session',
  :secret      => '3b3d5c7701f44cdad765b5b35c10de58434181f817def996d991cc4d071172b69e333df71dd67cf18c2acc0fb7e5e81d68842fd06981ba1857fa53a40f3ebb00'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
