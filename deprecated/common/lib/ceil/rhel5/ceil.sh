#! /bin/sh

CEIL_ISO_ROOT=/mnt/ceil
mkdir /root/.ssh

yes | cp ${CEIL_ISO_ROOT}/rhel5/authorized_keys /root/.ssh
yes | cp ${CEIL_ISO_ROOT}/rhel5/id_rsa /root/.ssh
yes | cp ${CEIL_ISO_ROOT}/rhel5/id_rsa.pub /root/.ssh

yes | cp ${CEIL_ISO_ROOT}/rhel5/network /etc/sysconfig/
yes | cp ${CEIL_ISO_ROOT}/rhel5/ifcfg_eth0 /etc/sysconfig/network-scripts/ifcfg-eth0
yes | cp ${CEIL_ISO_ROOT}/rhel5/resolv.conf /etc/

cat ${CEIL_ISO_ROOT}/rhel5/node.list >> /etc/hosts

service network restart

