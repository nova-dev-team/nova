#!/usr/bin/env python
#
# Downloads all depended .deb packages for Nova platform.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

import os

def apt_depends_helper(pkg_name):
  f = os.popen("apt-cache depends %s" % pkg_name)
  depends = []
  for line in f.readlines():
    if line.startswith("  "):
      if line.startswith("  Depends:"):
        pkg = line.split()[1]
        if not pkg.startswith("<"):
          depends.append(pkg)
      elif line.startswith("    "):
        pkg = line.strip()
        if not pkg.startswith("<"):
          depends.append(pkg)
      else:
        break
  f.close()
  print "%s:" % pkg_name,
  for pkg in depends:
    print pkg,
  print
  return depends

def apt_depends(deb_list):
  open_list = deb_list
  closed_list = []
  while len(open_list) > 0:
    pkg = open_list.pop()
    closed_list.append(pkg)
    for depend_pkg in apt_depends_helper(pkg):
      if depend_pkg not in closed_list and depend_pkg not in open_list:
        open_list.append(depend_pkg)
  return closed_list

"""
with open(os.path.dirname(__file__) + "") as f:

pkgs_input = raw_input("input packages in a line, separate with space:\n")
pkgs = pkgs_input.split()
pkgs_depend = apt_depends(pkgs)
pkgs_depend.sort()
print pkgs_depend
down_dir = raw_input("Download folder: ")
partial_folder = down_dir + os.path.sep + "partial"
if not os.path.exists(partial_folder):
  os.makedirs(partial_folder)
for pkg in pkgs_depend:
  cmd = "sudo apt-get install --reinstall -d -y -o Dir::Cache::Archives=%s %s" % (down_dir, pkg)
  print cmd
  os.system(cmd)
"""


required_debs_list_fn = os.path.dirname(__file__) + os.path.sep + "../data/debs.list"
pkgs = []
with open(required_debs_list_fn, "r") as f:
  for line in f.readlines():
    pkg_name = line.strip()
    if pkg_name == "":
      continue
    pkgs += pkg_name,
print pkgs
pkgs_depend = apt_depends(pkgs)
pkgs_depend.sort()
print pkgs_depend

full_debs_list_fn = os.path.dirname(__file__) + os.path.sep + "../data/debs.list.full"
with open(full_debs_list_fn, "w") as f:
  for pkg in pkgs_depend:
    f.write(pkg + "\n")

download_dir = os.path.dirname(__file__) + os.path.sep + "../data/debs"
partial_dir = download_dir + os.path.sep + "partial"
if not os.path.exists(partial_dir):
  os.makedirs(partial_dir)

for pkg in pkgs_depend:
  cmd = "sudo apt-get install --reinstall -d -y -o Dir::Cache::Archives=%s %s" % (download_dir, pkg)
  print "[cmd] %s" % cmd
  os.system(cmd)
