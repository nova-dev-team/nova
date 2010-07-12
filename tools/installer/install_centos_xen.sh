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

  echo =============================
  echo Phase 2: download source code
  echo =============================
  cd $SCRIPT_ROOT/data/src
  if [ -e "xen-3.3.1.tar.gz" ]
  then
    echo "Xen 3.3.1 already exists, skip downloading."
  else
    wget http://bits.xensource.com/oss-xen/release/3.3.1/xen-3.3.1.tar.gz
  fi
  if [ -e "kernel-2.6.18-164.15.1.el5.src.rpm" ]
  then
    echo "Kernel 2.6.18 for CentOS already exists, skip downloading."
  else
    wget http://mirror.centos.org/centos/5.4/updates/SRPMS/kernel-2.6.18-164.15.1.el5.src.rpm
  fi
  # get back to where I were
  cd $RUNNING_ROOT

  echo ===========================
  echo Phase 3: Unpack source code
  echo ===========================
  mkdir -p /tmp/nova_build
  cd $SCRIPT_ROOT/data/src
  cp xen-3.3.1.tar.gz /tmp/nova_build
  cd /tmp/nova_build
  tar xzf xen-3.3.1.tar.gz

  # get back to where I were
  cd $RUNNING_ROOT

  cd $SCRIPT_ROOT/data/src
  rpm -i kernel-2.6.18-164.15.1.el5.src.rpm
  cd /usr/src/redhat
  rpmbuild -bp --with xenonly SPECS/kernel-2.6.spec
  cp -r BUILD/kernel-2.6.18/linux-2.6.18.x86_64 /tmp/nova_build

  echo ================
  echo Phase 4: Compile
  echo ================
  
  cd /tmp/nova_build/xen-3.3.1
  make xen tools
  cd /tmp/nova_build/linux-2.6.18.x86_64
  cp configs/kernel-2.6.18-x86_64-xen.config .config
  make

  echo ================
  echo Phase 5: Install
  echo ================
  cd /tmp/nova_build/xen-3.3.1
  make install-xen install-tools
  cd /tmp/nova_build/linux-2.6.18.x86_64
  make modules_install install
  mkinitrd /boot/initrd-2.6.18-prep.img 2.6.18-prep

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
  echo "        kernel /xen-3.3.1.gz"
  echo "        module /vmlinuz-2.5.18-prep ro root=/dev/sda2"
  echo "        module /initrd-2.6.18-prep.img"
  echo 
  echo ====================================================================

  # get back to where I were
  cd $RUNNING_ROOT

else
  echo "Sorry, your Linux distribution is not supported!"
  exit 1
fi

