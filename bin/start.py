#!/usr/bin/env python

# Script to start Nova components
# Santa Zhang, 2011-07

import os
import sys

def print_help():
  print "Startup script for Nova"
  print "Usage: start.py [master|worker|agent|storge|client]"

if len(sys.argv) < 2 or "help" in sys.argv[1]:
  print_help()
  exit()
if sys.argv[1] == "master":
  nova_module = "nova.master.NovaMaster"
elif sys.argv[1] == "worker":
  nova_module = "nova.worker.NovaWorker"
elif sys.argv[1] == "agent":
  nova_module = "nova.agent.NovaAgent"
elif sys.argv[1] == "storage":
  nova_module = "nova.storage.NovaStorage"
elif sys.argv[1] == "client":
  nova_module = "org.apache.pivot.wtk.DesktopApplicationContext nova.ui.NovaUI"
else:
  print_help()
  exit(1)

# this script file's abs path
my_abs_path = os.path.abspath(os.path.join(os.getcwd(), __file__))

bin_dir = os.path.split(my_abs_path)[0]

nova_home = os.path.split(bin_dir)[0]

f = open(os.path.join(nova_home, "../VERSION"))
nova_ver = f.read().strip()
print "[INFO] Nova version: %s" % nova_ver
f.close()

if os.path.exists(os.path.join(bin_dir, "nova-%s.jar" % nova_ver)) == False:
  print "[ERROR] '%s' not found under '%s'" % ("nova-%s.jar" % nova_ver, bin_dir)
  exit(1)
fuyfuyfuyyu
lib_dir = os.path.join(nova_home, "lib")
conf_dir = os.path.join(nova_home, "conf")
db_dir = os.path.join(nova_home, "db")

java_home = os.getenv("JAVA_HOME")
if java_home == None:
  print "[WARN] JAVA_HOME not set!"
  # TODO use jre/ if JAVA_HOME not set

class_path = "conf;db"

orig_class_path = os.getenv("CLASSPATH")
if orig_class_path != None:
  class_path += ";%s" % orig_class_path
if java_home != None:
  class_path += ";%s" % java_home

for fn in os.listdir(lib_dir):
  if fn.startswith("."):
    continue
  if fn.lower().endswith(".jar"):
    class_path += ";lib/%s" % fn

class_path += ";bin/nova-%s.jar" % nova_ver

# create dirs if necessary
for dir_name in ["log", "run"]:
  dir_path = os.path.join(nova_home, dir_name)
  if os.path.exists(dir_path) == False:
    os.makedirs(dir_path)

os.system("cd %s" % (nova_home))
os.system("""java -server -classpath "%s" %s""" % (class_path, nova_module))
#print "nova_home:%s" % nova_home
#print "class_path:%s" % class_path
#print "nova_module:%s" % nova_module
print "++++++++++++++++++++++++++++++++++++"
