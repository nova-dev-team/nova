#!/usr/bin/python
#
# Makes all node ssh nopass in a cluster.
# 
# Author::      Santa Zhang (santa1987@gmail.com)
# Since::       0.3

import os
import random
import getpass

def brief_intro():
  print "Brief introduction to this script"
  print "TODO"


def has_root_privilege():
  return os.geteuid() == 0;


def node_list_required():
  nodelist_file = os.path.dirname(__file__) + os.path.sep + "nodelist.txt"
  if os.path.exists(nodelist_file):
    return
  else:
    print "Please provide a 'nodelist.txt' in the same folder with this script!"
    print "Each line in the file should be in this format:"
    print "<[user@]ip> [hostname1] [hostname2] ..."
    print
    print "eg."
    print "santa@10.0.0.1 n1-test"
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
  user = getpass.getuser()
  ip = None
  hostnames = []
  if "@" in splt[0]:
    user, ip = splt[0].split("@")
  else:
    ip = splt[0]
  if not is_valid_ipv4(ip):
    print "Skip invalid ip address: %s!" % ip
    return None
  for i in range(1, len(splt)):
    if is_valid_hostname(splt[i]):
      hostnames += splt[i],
    else:
      print "Skip invalid hostname '%s' for ip '%s'!" % (splt[i], ip)
  return (user, ip, hostnames)


def read_node_list():
  """Return a node list (actully a map), with ip as key, (set of hostnames, set of usernames) as value."""
  nodelist = {}
  nodelist_file = os.path.dirname(__file__) + os.path.sep + "nodelist.txt"
  print "Reading '%s'." % nodelist_file
  with open(nodelist_file, "r") as f:
    for line in f.readlines():
      ret = parse_node_info(line)
      if ret == None:
        continue
      user, ip, hostnames = ret
      if nodelist.has_key(ip):
        nodelist[ip][0].update([user])
        nodelist[ip][1].update(hostnames)
      else:
        nodelist[ip] = (set([user]), set(hostnames))
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
            for hostname in nodelist[splt[0]][1]:
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
        for hostname in nodelist[ip][1]:
          f.write(" %s" % hostname)
        f.write("\n")


def random_token(size = 5):
  alphabet = "abcdefghijklmnopqrstuvwxyz"
  token = ""
  for i in range(size):
    token += alphabet[random.randint(0, len(alphabet) - 1)]
  return token


def my_exec(cmd):
  print "[cmd] %s" % cmd
  os.system(cmd)


def config_ssh_nopass(nodelist):
  pub_fn = os.environ['HOME'] + "/.ssh/id_rsa.pub"
  auth_fn = os.environ['HOME'] + "/.ssh/authorized_keys"

  ssh_keygen_cmd = "if [ -e ~/.ssh/id_rsa.pub ] ; then echo 'ssh key exists' ; else ssh-keygen -N '' -f ~/.ssh/id_rsa ; fi"
  # make sure id_rsa.pub exists
  my_exec(ssh_keygen_cmd)

  my_pub_key = ""
  with open(pub_fn, "r") as f:
    my_pub_key = f.readline().strip()

  # self-to-remote ssh nopass
  for ip in nodelist.keys():
    user_set = nodelist[ip][0]
    for user in user_set:
      my_exec('ssh -o stricthostkeychecking=no %s@%s "(%s); echo \'%s\' >> ~/.ssh/authorized_keys"' % (user, ip, ssh_keygen_cmd, my_pub_key))

  # collect pub keys from remote server
  tmp_dir = "/tmp/nova-ssh-all2all.%s" % random_token()
  my_exec("mkdir -p %s" % tmp_dir)
  for ip in nodelist.keys():
    user_set = nodelist[ip][0]
    for user in user_set:
      if user == "root":
        my_exec("scp -o stricthostkeychecking=no %s@%s:/root/.ssh/id_rsa.pub %s/%s@%s.id_rsa.pub" % (user, ip, tmp_dir, user, ip))
      else:
        my_exec("scp -o stricthostkeychecking=no %s@%s:/home/%s/.ssh/id_rsa.pub %s/%s@%s.id_rsa.pub" % (user, ip, user, tmp_dir, user, ip))

  my_exec("cat %s/*.id_rsa.pub >> %s/all-keys.pub" % (tmp_dir, tmp_dir))
  my_exec("rm -f %s/*.id_rsa.pub" % tmp_dir)
  my_exec("cp nodelist.txt ssh_nopass_all2all.py ssh_nopass_cleanup.py %s" % tmp_dir)

  # all-to-all ssh nopass
  for ip in nodelist.keys():
    user_set = nodelist[ip][0]
    for user in user_set:
      token = random_token()
      my_exec("scp -r -o stricthostkeychecking=no %s %s@%s:/tmp/nova-ssh-all2all-remote.%s" % (tmp_dir, user, ip, token))
      my_exec('ssh -o stricthostkeychecking=no %s@%s "cd /tmp/nova-ssh-all2all-remote.%s && python ./ssh_nopass_all2all.py && rm -rf /tmp/nova-ssh-all2all-remote.%s"' % (user, ip, token, token))


def run_remote_mode():
  print "Running in remote mode"
  my_exec("cat all-keys.pub >> ~/.ssh/authorized_keys")
  my_exec("python ./ssh_nopass_cleanup.py")
  nodelist = read_node_list()
  if has_root_privilege():
    print "Running with root privilege, will update '/etc/hosts' file."
    update_etc_hosts(nodelist)

  print "Remote job finished"


if __name__ == "__main__":
  if os.path.exists("all-keys.pub"):
    run_remote_mode()
    exit()
  brief_intro()
  node_list_required()
  nodelist = read_node_list()
  config_ssh_nopass(nodelist)
  if has_root_privilege():
    print "Running with root privilege, will update '/etc/hosts' file."
    update_etc_hosts(nodelist)
  print "Everything done!"

