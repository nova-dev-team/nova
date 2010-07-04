# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_nfs_storage_server_session',
  :secret      => 'd6a6327901e6dcff8a19ad155eafe15be6b7adfd24b8975a8da9f71c2368f44927ab6731eaa39f5489f98419c52fa63662fd3e2eb11bec8f23324e71e5a7ac6f'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
