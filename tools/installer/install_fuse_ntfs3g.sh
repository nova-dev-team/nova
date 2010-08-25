#!/bin/bash

# This script will install FUSE and ntfs-3g support for NTFS drivers.
#
# Author::  Santa Zhang (santa1987@gmail.com)

# check root privilege
if [[ $UID -ne 0 ]]; then
  echo "This script requires root privilege!"
  exit 1
fi

clear
echo
echo This script will install FUSE component and ntfs-3g support for NTFS drivers.
echo
read -p "Press ENTER to start installation, or press Ctrl+C to quit..."
clear

# where is the script?
SCRIPT_ROOT=$(dirname $0)
RUNNING_ROOT=$(pwd)

echo ==================================
echo Phase 1: install fuse
echo ==================================
cd $RUNNING_ROOT
cp $SCRIPT_ROOT/data/src/fuse-2.8.4.tar.gz /tmp
cd /tmp
tar zxf fuse-2.8.4.tar.gz
cd fuse-2.8.4
./configure --prefix=/usr
make
make install
modprobe fuse
cd /tmp
rm -rf fuse-2.8.4 fuse-2.8.4.tar.gz

echo =============================
echo Phase 2: install ntfs-3g
echo =============================
cd $RUNNING_ROOT
cp $SCRIPT_ROOT/data/src/ntfs-3g-2010.8.8.tgz /tmp
cd /tmp
tar zxf ntfs-3g-2010.8.8.tgz
cd ntfs-3g-2010.8.8
./configure
make
make install
cd /tmp
rm -rf ntfs-3g-2010.8.8 ntfs-3g-2010.8.8.tgz
cd $RUNNING_ROOT

echo Everything done!

