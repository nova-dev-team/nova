

require File.dirname(__FILE__) + "/../launcher/config"


cc = ClusterConfiguration.new("192.168.0.110")
cc.fetch_by_http("192.168.0.110")

puts cc.inst_list
puts cc.node_list

