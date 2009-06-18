# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_pnode.v2_session',
  :secret      => '3e368c7bf76b19a18dba70996cca8fe0ce7817c14032487913734c3f92a9a65a36097414339e693f987b9e0c8bace6be085624f153360b629aa3456ce2c4a6a3'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
