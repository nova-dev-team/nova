#!/usr/bin/python

# this file is used to generate test data in Nova

from urllib2 import *

import os

import json

str = os.popen("ifconfig | grep 'inet addr' | awk '{print $2}'").read().split('\n')[0]
ip = str[str.find(':') + 1:]

# ip = "localhost"

def invoke(rest):
  global ip
  url = "http://%s:3000/%s" % (ip, rest)
  print "Invoking", url
  u = urlopen(url)
  ret = u.read()
  print ret
  print # new line
  return json.loads(ret)
  
invoke('user/add/santa')
invoke('user/add/jiong')
invoke('user/add/rocks')
invoke('user/list')

invoke('vcluster/create') # create c1
invoke('vcluster/create') # create c2
invoke('vcluster/create') # create c3
invoke('vcluster/list')

invoke('pmachine/add/10.0.2.3')
invoke('pmachine/add/10.0.2.4')
invoke('pmachine/add/10.0.2.5')
invoke('pmachine/list')

invoke('vmachine/create') # create v1
invoke('vmachine/create') # create v2
invoke('vmachine/create') # create v3
invoke('vmachine/list')

# hosting function is not to be used
#invoke('pmachine/host_vmachine/10.0.2.3/v1')
#invoke('pmachine/host_vmachine/10.0.2.3/v2')
#invoke('pmachine/host_vmachine/10.0.2.3/v3')
#invoke('pmachine/info/10.0.2.3')

