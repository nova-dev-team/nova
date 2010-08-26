#!/usr/bin/python

# This script creates a tar.gz package in nova source code root, named "nova.tar.gz".
# Git is required.
#
# Usage::
#   ./make_tar.py [tag_name]
#
# Author::  Santa Zhang (santa1987@gmail.com)
# Since::   0.3

import random
import os
import sys

def my_exec(cmd):
  print "[cmd] %s" % cmd
  os.system(cmd)

def rand_str(length = 5):
  str = ""
  alphabet = "abcdefghijklmnopqrstuvwxyz1234567890"
  for i in range(length):
    str += alphabet[random.randint(0, len(alphabet) - 1)]
  return str

print """
This tool will pack nova source code into a .tar.gz file.
Usage: ./make_tar.py [tag_name]
If tag_name is not provided, this tool will pack source code in CURRENT branch.
"""

tag_name = None
commit_id = None

# if provided tag name
if len(sys.argv) > 1:
  tag_name = sys.argv[1]
  pipe = os.popen("git tag")
  tags = []
  for line in pipe.readlines():
    tags += line.strip(),
  pipe.close()
  if tag_name not in tags:
    print "[error] tag '%s' not found!" % tag_name
    exit()
else:
  # no tag name provided, we use the 'commit-id' instead (first 8 chars in hash)
  pipe = os.popen("git log")
  line = pipe.readline()
  pipe.close()
  commit_id = line.split()[1][:8]

# git clone to a temp dir in "/tmp"
clone_folder_parent = "/tmp/nova-make-tar." + rand_str()
if tag_name != None:
  clone_folder = clone_folder_parent + "/nova-" + tag_name
else:
  clone_folder = clone_folder_parent + "/nova"
source_root = os.getcwd() + os.path.sep + os.path.split(__file__)[0] + "/../../"
print source_root
my_exec("git clone %s %s" % (source_root, clone_folder))
if tag_name != None:
  my_exec("cd %s && git checkout %s" % (clone_folder, tag_name))
my_exec("rm -Rf %s/.git" % clone_folder)

if tag_name != None:
  tar_fn = "%s/nova-%s.tar.gz" % (source_root, tag_name)
else:
  tar_fn = "%s/nova-snapshot-%s.tar.gz" % (source_root, commit_id)
my_exec("rm -f %s" % tar_fn)
if tag_name != None:
  my_exec("cd %s && tar pczf %s nova-%s" % (clone_folder + "/..", tar_fn, tag_name))
else:
  my_exec("cd %s && tar pczf %s nova" % (clone_folder + "/..", tar_fn))
my_exec("rm -Rf %s" % clone_folder_parent)

