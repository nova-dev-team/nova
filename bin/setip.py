#!/usr/bin/env python

import os
import sys

def merge_path(*paths):
    if os.name == "posix":
        sep = ":"
    else:
        sep = ";"
    return sep.join(paths)

# this script file's abs path
my_abs_path = os.path.abspath(__file__)

bin_dir = os.path.dirname(my_abs_path)

nova_home = os.path.abspath(os.path.join(bin_dir, ".."))

# set ip
file = open(os.path.join(nova_home,"../params/ipconfig.txt"))
ip = file.readline()
mask = file.readline()
gw = file.readline()
hostname = file.readline()
print ip,mask,gw,hostname
cmd = 'ifconfig eth0 %s netmask %s' % (ip,mask)
os.system(cmd)
cmd = 'route set default gw %s' % gw
os.system(cmd)
cmd = 'hostname %s' % hostname
os.system(cmd)
