#!/usr/bin/python

# This script creates a tar.gz package in nova source code root, named "nova.tar.gz".
# Git is required.
#
# Author::  Santa Zhang (santa1987@gmail.com)
# Since::   0.3

import random
import os

def my_exec(cmd):
  print "[cmd] %s" % cmd
  os.system(cmd)

def rand_str(length = 5):
  str = ""
  alphabet = "abcdefghijklmnopqrstuvwxyz1234567890"
  for i in range(length):
    str += alphabet[random.randint(0, len(alphabet) - 1)]
  return str

# git clone to a temp dir in "/tmp"
clone_folder_parent = "/tmp/nova-make-tar." + rand_str()
clone_folder = clone_folder_parent + "/nova"
source_root = os.getcwd() + os.path.sep + os.path.split(__file__)[0] + "/../../"
print source_root
my_exec("git clone %s %s" % (source_root, clone_folder))
my_exec("rm -Rf %s/.git" % clone_folder)

tar_fn = "%s/nova.tar.gz" % source_root
my_exec("rm -f %s" % tar_fn)
my_exec("cd %s && tar pczf %s nova" % (clone_folder + "/..", tar_fn))
my_exec("rm -Rf %s" % clone_folder_parent)

