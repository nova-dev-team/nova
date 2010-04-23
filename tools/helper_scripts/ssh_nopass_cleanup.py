#!/usr/bin/python
#
# Removes duplicated entries in "$HOME/.ssh/authorized_keys".
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

import os

def ssh_nopass_cleanup(home_folder):
  auth_key_file = "%s/.ssh/authorized_keys" % home_folder
  if os.path.exists(auth_key_file):
    other_lines = []
    ssh_lines = {}
    print "Backup '%s' to '%s~'." % (auth_key_file, auth_key_file)
    os.system("cp %s %s~" % (auth_key_file, auth_key_file)) # do backup
    print "Cleaning duplicated authorized keys."
    duplicate_counter = 0
    with open(auth_key_file, "r") as f:
      for line in f.readlines():
        if line.startswith("ssh-rsa") or line.startswith("rsa"):
          if ssh_lines.has_key(line):
            duplicate_counter += 1
          else:
            ssh_lines[line] = 1
        else:
          other_lines += line,
    if duplicate_counter == 0:
      print "No duplicate entry found in '%s'." % auth_key_file
    else:
      print "%d duplicate entries found in '%s'." % (duplicate_counter, auth_key_file)
      with open(auth_key_file, "w") as f:
        for line in ssh_lines.keys():
          f.write(line)
        for line in other_lines:
          f.write(line)
      print "Re-written '%s'." % auth_key_file
  else: # "authorized_keys" file not found
    print "File not found: %s" % auth_key_file


if __name__ == "__main__":
  home_dir = os.environ["HOME"]
  print "This script removes duplicated entries in '%s/.ssh/authorized_keys'." % home_dir
  ssh_nopass_cleanup(home_dir)
  print "Done!"

