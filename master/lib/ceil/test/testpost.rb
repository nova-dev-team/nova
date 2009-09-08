require 'net/http'

conn = Net::HTTP.new('localhost', 3000)


params = {}
params["log_type"] = "gundam"
params["log_msg"] = "RX-78 start up!"

puts params["log_type"]
puts params["log_msg"]

conn = Net::HTTP.new('localhost', 3000)
resp, data = conn.post("/ceil/report", params)

puts data
