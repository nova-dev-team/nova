#!/usr/bin/ruby

def sys_exec cmd
    puts cmd
    `#{cmd}`
end

(17..20).each do |i|
    sys_exec "iptables -t nat -A PREROUTING -d 166.111.131.32 -p tcp --dport 30#{i} -j DNAT --to-destination 10.0.5.#{i}:3000"
    sys_exec "iptables -t nat -A POSTROUTING -d 10.0.5.#{i} -p tcp --dport 3000 -j SNAT --to 10.0.5.254"
end

sys_exec "iptables-save"

