#
# $Id: kfs_setup.py 22 2007-09-25 20:09:24Z sriramsrao $
#
# Use the distutils setup function to build and install the KFS module.
# Execute this as:
#  python kfs_setup.py ~/code/kfs/build/lib/ build
# and this will build kfs.so in ./build/.../kfs.so
# This needs to be installed /usr/lib64/python/site-packages or in an
# alternate location; see COMPILING for instructions
# In addition, ~/code/kfs/build/lib needs to be in the LD_LIBRARY_PATH
# After installation, python apps can access kfs.
#
from distutils.core import setup, Extension
import sys

kfs_lib_dir = sys.argv[1]
del sys.argv[1]

kfsext = Extension('kfs',
		include_dirs = ['/home/sriram/code/kosmosfs/src/cc/'],
		libraries = ['kfsClient'],
		library_dirs = [kfs_lib_dir],
		sources = ['KfsModulePy.cc'])

setup(name = "kfs", version = "0.1",
	description="KFS client module",
	author="Blake Lewis",
	ext_modules = [kfsext])
