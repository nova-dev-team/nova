require File.dirname(__FILE__) + '/../server/dispatch_key'

kd = KeyDispatcher.new("blah", "common")
node_list = "10.0.0.2 test1\n10.0.0.3 test2"

kd.dispatch(node_list)

kd = KeyDispatcher.new("blah", "ssh-nopass")
node_list = "10.0.0.2 test1\n10.0.0.3 test2"

kd.dispatch(node_list)

