# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_worker_session',
  :secret      => '1759cdff441d78ff942b2756251b9a63dd2beac243a6a67861737fce2a1a4c0dd811bc0be04c9308e4602dbe1f81fea89e3911dcec326fce328c601e72f7ce1e'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
