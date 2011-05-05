# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_nss_session',
  :secret      => '1faeccebb589ef5f78d647d1577c39e6cd35a2725eafa8f32221a0b2460dc5dde34abb9dface8162b4f1fb96b7890e402093eb7dc7ed4fce94b45968852d35b2'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
