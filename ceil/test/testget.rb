require 'net/http'
require 'rubygems'
require 'json'

h = Net::HTTP.new('localhost',3000)
resp, data = h.get('/configurations.json', nil);

puts "hello"
puts data
puts "now"
puts JSON.parse(data)

