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
  cd $SCRIPT_ROOT/data/src
  cp xen-3.3.1.tar.gz /tmp
  cd /tmp
  tar xzf xen-3.3.1.tar.gz
  # get back to where I were
  cd $RUNNING_ROOT
  cd $SRCIPT_ROOT/data/src
  rpm -i kernel-2.6.18-164.15.1.el5.src.rpm
  cd /usr/src/redhat
  rpmbuild -bp --with xenonly SPECS/kernel-2.6.spec
  mkdir /tmp/kernel_src
  cp -r BUILD/kernel-2.6.18/linux-2.6.18.x86_64 /tmp/kernel_src

  echo ================
  echo Phase 4: Compile
  echo ================
  
  cd /tmp/xen-3.3.1
  make xen tools
  cd /tmp/kernel_src
  cp configs/kernel-2.6.18-x86_64-xen.config .config
  make


  # get back to where I were
  cd $RUNNING_ROOT

else
  echo "Sorry, your Linux distribution is not supported!"
  exit 1
fi

