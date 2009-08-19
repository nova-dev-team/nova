


require 'net/http'

h = Net::HTTP.new('localhost',3000)
data = h.get('/nodes.json', nil);

puts data



