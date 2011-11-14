#!/usr/bin/env python

# Script to start Nova components
# Santa Zhang, 2011-07

import os
import sys


def print_help():
    print "Startup script for Nova"
    print "Usage: start.py [master|worker|agent|storge|client]"


def merge_path(*paths):
    if os.name == "posix":
        sep = ":"
    else:
        sep = ";"
    return sep.join(paths)


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
    nova_module = "nova.agent.ui.AgentFrameUserStart"
else:
    print_help()
    exit(1)

# this script file's abs path
my_abs_path = os.path.abspath(__file__)

bin_dir = os.path.dirname(my_abs_path)
nova_home = os.path.abspath(os.path.join(bin_dir, ".."))

# search for VERSION file
nova_ver = None
cur_dir = nova_home
while True:
    ver_fn = os.path.join(cur_dir, "VERSION")
    if os.path.exists(ver_fn):
        f = open(ver_fn)
        nova_ver = f.read().strip()
        print "[INFO] Nova version: %s" % nova_ver
        f.close()
        break
    else:
        parent_dir = os.path.abspath(os.path.join(cur_dir, ".."))
        if parent_dir != cur_dir:
            cur_dir = parent_dir    # search in parent dir
        else:
            break

if nova_ver == None:
    print "[ERROR] cannot determine Nova version!"
    exit(1)

if os.path.exists(os.path.join(bin_dir, "nova-%s.jar" % nova_ver)) == False:
    print "[ERROR] '%s' not found under '%s'!" % ("nova-%s.jar" % nova_ver, bin_dir)
    exit(1)

lib_dir = os.path.join(nova_home, "lib")
conf_dir = os.path.join(nova_home, "conf")
db_dir = os.path.join(nova_home, "db")

java_home = os.getenv("JAVA_HOME")
if java_home == None:
    # just give a warning, don't quit yet
    # Mac's JAVA_HOME variable cannot be found, but Java works correctly
    print "[WARN] JAVA_HOME not set!"

# set proper CLASSPATH variable
class_path = merge_path("conf", "db")

orig_class_path = os.getenv("CLASSPATH")
if orig_class_path != None:
    class_path = merge_path(class_path, orig_class_path)

if java_home != None:
    class_path = merge_path(class_path, java_home)

for fn in os.listdir(lib_dir):
    if fn.startswith("."):
        continue
    if fn.lower().endswith(".jar"):
        class_path = merge_path(class_path, "lib/%s" % fn)

class_path = merge_path(class_path, "bin/nova-%s.jar" % nova_ver)


# create dirs if necessary
for dir_name in ["log", "run"]:
    dir_path = os.path.join(nova_home, dir_name)
    if os.path.exists(dir_path) == False:
        os.makedirs(dir_path)

os.chdir(nova_home)
os.system('java -server -classpath "%s" %s' % (class_path, nova_module))

