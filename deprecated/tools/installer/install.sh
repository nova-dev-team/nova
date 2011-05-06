#!/bin/bash

# Installs the Nova platform (master module).
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

# check root privilege
if [[ $UID -ne 0 ]]; then
  echo "This script requires root privilege!"
  exit 1
fi

clear
echo
echo "This script installs the Nova platform."
echo "Currently it only supports Ubuntu and CentOS distribution."
echo
echo You may want to modify '../../common/config/conf.yml' according to
echo your needs.
echo
echo "The installation process:"
echo "1: Install depended software packages"
echo "2: Install depended rubygems packages"
echo "3: Compile utility tools"
echo "4: Install Nova master module"
echo "5: Create worker node installer"
echo "6: Configure the Nova system"
echo "7: Install Nova worker module"
echo
echo This script does steps 1~4 for you. You will have to do step 5~7
echo by hand. See README for more info.
echo
read -p "Press ENTER to start installation, or press Ctrl+C to quit..."

clear
echo "Phase 1: Installing depended software packages..."
echo

# where is the script?
SCRIPT_ROOT=$(dirname $0)
RUNNING_ROOT=$(pwd)

# determine distribution, and do corresponding installing work
if grep -q "Ubuntu" "/etc/issue" ; then
  echo "Current Linux distribution is Ubuntu."

  DEBS_LIST=$SCRIPT_ROOT/data/debs.list
  DEBS_DIR=$SCRIPT_ROOT/data/debs

  apt-get update
  all_debs=( $( cat $DEBS_LIST ) )
  apt-get install -y ${all_debs[@]} || exit 1

elif grep -q "CentOS" "/etc/issue" ; then
  echo "Current Linux distribution is CentOS."
  yum makecache
  yum groupinstall -y "Development Tools"

  # install a few packages directly by yum
  YUM_LIST=$SCRIPT_ROOT/data/yum.list
  all_yum=( $( cat $YUM_LIST ) )
  yum install -y ${all_yum[@]} || exit 1

  # install ruby from src directory
  cp $SCRIPT_ROOT/data/src/ruby-1.8.7-p249.tar.gz /tmp
  cd /tmp
  tar zxf ruby-1.8.7-p249.tar.gz
  cd ruby-1.8.7-p249
  ./configure --prefix=/usr
  make
  make install

  # compile readline
  cd ext/readline
  ruby extconf.rb
  make
  make install

  # rm ruby source code
  cd /tmp
  rm -rf ruby-1.8.7-p249 ruby-1.8.7-p249.tar.gz

  # get back to where I were
  cd $RUNNING_ROOT

else
  echo "Sorry, your Linux distribution is not supported!"
  exit 1
fi

# switch to ruby script
echo
ruby $SCRIPT_ROOT/lib/install_stage2.rb

