#!/usr/bin/env ruby

# Run the daemons like this:
#
#   RAILS_ENV=production ./script/daemons start

ENV["RAILS_ENV"] ||= "production"

require File.dirname(__FILE__) + "/../../config/environment"

$running = true
Signal.trap("TERM") do 
  $running = false
end

while($running) do
  # pusher does nothing, hiahiahia~  
  sleep 10
end
