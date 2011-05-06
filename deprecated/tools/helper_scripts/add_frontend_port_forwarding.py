#!/usr/bin/python

print "This script does nothing except giving you some info."
print "To enable ip forwarding, run the following commands:"
print
print "  sysctl -w net.ipv4.ip_forward=1"
print "  /sbin/iptables -P FORWARD ACCEPT"
print "  /sbin/iptables --table nat -A POSTROUTING -o eth1 -j MASQUERADE (eth1 has Internet ip)"
print

