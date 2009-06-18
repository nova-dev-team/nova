# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_core.v2_session',
  :secret      => '0113b9ae45777a8f0aa1b4b75630b7cddd0e589d8dec794737b6d320993e99662121d9057387ac4af430705623fb9e26131960c60b8ba84951d60be0d6f6e297'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
