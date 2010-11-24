#!/bin/bash

# This script is for research purpose only!
# It installs Xen 3.3.1 on CentOS 5.4 from source code.
# This script requires Internet connection.
#
# Author::  Santa Zhang (santa1987@gmail.com)

# check root privilege
if [[ $UID -ne 0 ]]; then
  echo "This script requires root privilege!"
  exit 1
fi

clear
echo
echo This script is for research purpose only!
echo It installs Xen 3.3.1 on CentOS 5.4 from source code.
echo This script requires Internet connection.
echo Source code will be compiled at '/tmp/nova_build'
echo
read -p "Press ENTER to start installation, or press Ctrl+C to quit..."
clear

# where is the script?
SCRIPT_ROOT=$(dirname $0)
RUNNING_ROOT=$(pwd)

if grep -q "CentOS" "/etc/issue" ; then
  echo "Current Linux distribution is CentOS."
  echo ==================================
  echo Phase 1: prepare depended packages
  echo ==================================
  yum makecache
  yum groupinstall -y "Development Tools"

  # install a few packages directly by yum
  YUM_LIST=$SCRIPT_ROOT/data/yum.list
  all_yum=( $( cat $YUM_LIST ) )
  yum install -y ${all_yum[@]} || exit 1


  # get back to where I were
  cd $RUNNING_ROOT

  echo ===========================
  echo Phase 2: Unpack source code
  echo ===========================
  mkdir -p /tmp/nova_build
  cd $SCRIPT_ROOT/data/src
  cp xen_hotbackup.tar.bz2 /tmp/nova_build
  cd /tmp/nova_build
  tar xvf xen_hotbackup.tar.bz2

  echo ================
  echo Phase 3: Install
  echo ================
  cd $SCRIPT_ROOT/data/src
  rpm -i dmraid-devel-1.0.0.rc13-63.e15.x86_64.rpm
  cd /tmp/nova_build/xen_hotbackup
  yes "" | make install
  cd /tmp/nova_build/xen_hotbackup/tools
  make clean
  make
  make install
  cd /tmp/nova_build/xen_hotbackup/build-linux-2.6.18-xen_x86_64
  make modules_install
  make install
  mkinitrd /boot/initrd-2.6.18.8-xen.img 2.6.18.8-xen
  yun install libvirt
  cd /tmp/nova_build/xen_hotbackup
  yes "" | make install
  cd /tmp/nova_build/xen_hotbackup/tools
  make clean
  make
  make install

  echo ====================================================================
  echo
  echo "*** Finished installing Xen and Linux kernel."
  echo
  echo The compiled binary is in '/tmp/nova_build' directory.
  echo Now, you need to config the Grub loader.
  echo Here is an example Grub config file:
  echo "(sda1 as boot partition, sda2 as main partition)"
  echo 
  echo title Xen
  echo "        root(hd0,0)"
  echo "        kernel /boot/xen.gz"
  echo "        module /boot/vmlinuz-2.6.18.8-xen ro root=LABEL=/1 rhgh quiet"
  echo "        module /boot/initrd-2.6.18.8-xen.img"
  echo 
  echo ====================================================================

  # get back to where I were
  cd $RUNNING_ROOT

else
  echo "Sorry, your Linux distribution is not supported!"
  exit 1
fi

