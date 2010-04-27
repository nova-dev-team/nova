#!/usr/bin/python

import os

lst = []

with os.popen("dpkg --list") as f:
  for line in f.readlines():
    line = line.strip()
    splt = line.split()
    if len(splt) > 2:
      pkg = splt[1]
      print pkg
      lst += pkg,

print
print "================================================"
print
lst.sort()
save_fn = raw_input("Save to file: ")
if save_fn != "":
  with open(save_fn, "w") as f:
    for item in lst:
      f.write(item + "\n")

