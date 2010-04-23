#!/usr/bin/python
#
# Makes all node ssh nopass in a cluster.
# 
# Author::      Santa Zhang (santa1987@gmail.com)
# Since::       0.3

import os

def brief_intro():
  print "Brief introduction to this script"
  print "TODO"


def require_root_privilege():
  if os.geteuid() == 0:
    return
  else:
    print "This script requires root privilege!"
    exit()


def node_list_required():
  nodelist_file = os.path.dirname(__file__) + os.path.sep + "nodelist.txt"
  if os.path.exists(nodelist_file):
    return
  else:
    print "Please provide a 'nodelist.txt' in the same folder with this script!"
    print "Each line in the file should be in this format:"
    print "<ip> [hostname1] [hostname2] ..."
    print
    print "eg."
    print "10.0.0.1 node1 n1"
    print "10.0.0.2 node2 test-node"
    print "10.0.0.3"
    print "10.0.0.4 gateway"
    exit()


def is_valid_ipv4(ip_str):
  splt = ip_str.split(".")
  if len(splt) != 4:
    return False
  for seg in splt:
    if seg.isdigit():
      v = int(seg)
      if not (0 <= v and v <= 255):
        return False
    else:
      return False
  return True


def is_valid_hostname(name_str):
  if len(name_str) == 0:
    return False
  if not name_str[0].isalpha():
    # hostname must start with a-zA-Z
    return False
  for c in name_str:
    # only allow a-zA-Z0-9-_
    if not (c.isalpha() or c.isdigit() or c == "-" or c == "_"):
      return False
  return True


def parse_node_info(line):
  """Parse a line in node list file, or in magic comments of '/etc/hosts'."""
  splt = line.split()
  if len(splt) == 0:
    # skip empty lines
    return
  ip = None
  hostnames = []
  if is_valid_ipv4(splt[0]):
    ip = splt[0]
  else:
    print "Skip invalid ip address: %s!" % splt[0]
    return
  for i in range(1, len(splt)):
    if is_valid_hostname(splt[i]):
      hostnames += splt[i],
    else:
      print "Skip invalid hostname '%s' for ip '%s'!" % (splt[i], ip)
      return
  return (ip, hostnames)


def read_node_list():
  """Return a node list (actully a map), with ip as key, and a list of hostnames as value."""
  nodelist = {}
  nodelist_file = os.path.dirname(__file__) + os.path.sep + "nodelist.txt"
  print "Reading '%s'." % nodelist_file
  with open(nodelist_file, "r") as f:
    for line in f.readlines():
      ret = parse_node_info(line)
      if ret == None:
        continue
      ip, hostnames = ret
      if nodelist.has_key(ip):
        nodelist[ip].extend(hostnames)
      else:
        nodelist[ip] = hostnames
  return nodelist


def update_etc_hosts(nodelist):
  """Update '/etc/hosts' according to nodelist. Root privilege assumed."""
  print "Parsing '/etc/hosts'."
  etc_hosts = "/etc/hosts"
  magic_header = "# by ssh_nopass_all2all.py:"
  file_info = [] # will contain (line, info) pairs
  info = None
  with open(etc_hosts, "r") as f:
    for line in f.readlines():
      if line.strip().startswith(magic_header):
        # retireve info on magic comment lines
        info_str = line[len(magic_header):]
        exec "info = %s" % info_str # tricky way to parse info list

      elif line.strip().startswith("#"):
        # normal comment lines
        info = None # clears info
        file_info += (line, info), 

      else:
        # handles general lines
        splt = line.split()
        if len(splt) != 0:
          if nodelist.has_key(splt[0]):
            new_hostnames = []
            for hostname in nodelist[splt[0]]:
              if hostname not in splt[1:]:
                new_hostnames += hostname,
            if len(new_hostnames) != 0:
              print "Adding new hostnames", new_hostnames, "to '%s'" % splt[0]
              line = line.strip()
              for new_hostname in new_hostnames:
                line += " %s" % new_hostname
              line += "\n"
              info = new_hostnames # set info to new hostnames
            nodelist[splt[0]] = None  # clean it up
        file_info += (line, info),
        info = None # clears info for following lines
  # write back to /etc/hosts
  with open(etc_hosts, "w") as f:
    for info_pair in file_info:
      line, info = info_pair
      if info != None:
        f.write("%s %s\n" % (magic_header, str(info)))
      f.write(line)
    for ip in nodelist:
      if nodelist[ip] != None:
        # a tricky way to denote new node
        f.write(ip)
        for hostname in nodelist[ip]:
          f.write(" %s" % nodelist[ip])
        f.write("\n")


if __name__ == "__main__":
  brief_intro()
  require_root_privilege()
  node_list_required()
  nodelist = read_node_list()
  update_etc_hosts(nodelist)
  # TODO update "authorized_keys" on different nodes

