#!/usr/bin/ruby

require 'nodelist'

all_node_name do |n|
    i = n[4..-1].to_i
    sys_exec "iptables -t nat -A PREROUTING -d 166.111.131.32 -p tcp --dport #{3000 + i} -j DNAT --to-destination 10.0.5.#{i}:3000"
    sys_exec "iptables -t nat -A POSTROUTING -d 10.0.5.#{i} -p tcp --dport 3000 -j SNAT --to 10.0.5.254"
end

sys_exec "iptables -P FORWARD ACCEPT"
sys_exec "iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE"

sys_exec "iptables-save"

