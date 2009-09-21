#!/bin/bash

note=`date +"%Y-%m-%d-%H-%M-%S"`
mv /etc/network/interfaces /etc/network/interfaces.backup.${note}
mv /etc/dhcp3/dhcpd.conf /etc/dhcp3/dhcpd.conf.backup.${note}

cp tmp/interfaces /etc/network/
cp tmp/dhcpd.conf /etc/dhcp3/

/etc/init.d/dhcp3-server restart
echo "networking need restart, type \"/etc/init.d/networking restart\""

