#!/usr/bin/python

# This script creates a tar.gz package in git scm source code root.
# Git is required.
#
# Usage::
#   ./make_tar.py [tag_name]
#
# Author::  Santa Zhang (santa1987@gmail.com)

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
This tool will pack git scm source code into a .tar.gz file.
Usage: ./make_tar.py [tag_name]
If tag_name is not provided, this tool will pack source code in CURRENT branch.
"""

tag_name = None
commit_id = None

def my_exec_read_one_line(cmd):
  pipe = os.popen(cmd)
  line = pipe.readline()
  pipe.close()
  return line

def get_tags():
  pipe = os.popen("git tag")
  tags = []
  for line in pipe.readlines():
    tags += line.strip(),
  pipe.close()
  return tags

# if provided tag name
if len(sys.argv) > 1:
  tag_name = sys.argv[1]
  tags = get_tags()
  if tag_name not in tags:
    print "[error] tag '%s' not found!" % tag_name
    exit()
else:
  # try to get the tag name from git show
  current_commit_hash = my_exec_read_one_line("git show")
  tags = get_tags()
  for tag in tags:
    tagged_commit_hash = my_exec_read_one_line("git show %s" % tag)
    if tagged_commit_hash == current_commit_hash:
      tag_name = tag
      break

  if tag_name == None:
    # no tag name found, we use the 'commit-id' instead (first 8 chars in hash)
    line = my_exec_read_one_line("git log")
    commit_id = line.split()[1][:8]

# get the project name
proj_name = None
check_dir = os.path.split(os.path.realpath(__file__))[0]
while check_dir != "/":
  if ".git" in os.listdir(check_dir):
    proj_name = os.path.split(check_dir)[1]
    source_root = check_dir
    break
  check_dir = os.path.split(check_dir)[0]

if proj_name == None:
  print "[fatal] cannot find .git directory in any parent directory! not a valid git repository!"
  exit(1)

# git clone to a temp dir in "/tmp"
clone_folder_parent = "/tmp/%s-make-tar.%s" % (proj_name, rand_str())
if tag_name != None:
  clone_folder = clone_folder_parent + ("/%s-" % proj_name) + tag_name
else:
  clone_folder = clone_folder_parent + os.path.sep + proj_name
print source_root
my_exec("git clone %s %s" % (source_root, clone_folder))
if tag_name != None:
  my_exec("cd %s && git checkout %s" % (clone_folder, tag_name))
my_exec("rm -Rf %s/.git" % clone_folder)

if tag_name != None:
  tar_fn = "%s/%s-%s.tar.gz" % (source_root, proj_name, tag_name)
else:
  tar_fn = "%s/%s-snapshot-%s.tar.gz" % (source_root, proj_name, commit_id)
my_exec("rm -f %s" % tar_fn)
if tag_name != None:
  my_exec("cd %s && tar pczf %s %s-%s" % (clone_folder + "/..", tar_fn, proj_name, tag_name))
else:
  my_exec("cd %s && tar pczf %s %s" % (clone_folder + "/..", tar_fn, proj_name))
my_exec("rm -Rf %s" % clone_folder_parent)

