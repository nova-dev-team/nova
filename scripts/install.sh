#!/bin/bash

SINGLE_NODE=false

for opt in $@; do
  case "$opt" in
  --single-node)
    SINGLE_NODE=true
  ;;
  --help)
    echo "This is the install script for Nova system. Currently it only supports Ubuntu distribution."
    echo "Root privilege required."
    echo "usage:"
    echo ""
    echo "install.sh"
    echo "    -- will install the whole system"
    echo ""
    echo "install.sh --single-node"
    echo "    -- will install this node only"
    exit 0
  ;;
  esac
done

# must run with root privilege
if [[ $UID -ne 0 ]]; then
  echo "This script requires root privilege!"
  exit 1
fi

if [[ $SINGLE_NODE == false ]]; then
  clear
  echo ""
  echo "This script installs the Nova platform. Currently it only supports Ubuntu distribution."
  # TODO check distribution == ubuntu
  echo ""
  echo "The installation process:"
  echo " 1: Install depended software and libraries."
  echo " 2: A few questions will be asked for basic configuration. *(Your instructions required)"
  echo " 3: This node will be installed as 'master' machine."
  echo " 4: Other nodes will be automatically installed."
  echo ""
  echo "Your instructions are required in step 2 only. After that, no more instructions are required."
  echo "After the installation process, most of the configuration work is done."
  echo "You need to do a few more configuration work, though. See README for detail information."
  echo ""
  read -p "Press any key to start installation..."
else
  echo "Install single node only"
fi

# phase 1: download packages
if [[ $SINGLE_NODE == false ]]; then
  clear
fi

echo "Phase 1: Download .deb packages..."
echo ""

DEBS_LIST_FILE=$(readlink -f ./config/installer/debs.list)
PACKAGE_ROOT=$(readlink -f ./data/installer)
DEBS_PACKAGE_DIR=$PACKAGE_ROOT/debs

mkdir -p $DEBS_PACKAGE_DIR/archives/partial
all_debs=( $( cat $DEBS_LIST_FILE ) )
(yes | apt-get install --reinstall -d -o dir::cache=$DEBS_PACKAGE_DIR ${all_debs[@]}) || exit 1

# Phase 2: begin installing process

if [[ $SINGLE_NODE == false ]]; then
  clear
fi

echo "Phase 2: Install packages..."
echo ""

echo "Phase 2.1: Install .deb packages..."
echo ""
cd $PACKAGE_ROOT/debs/archives
dpkg -i --skip-same-version *.deb

if [ ! -e /usr/local/bin/ruby ]; then
  ln /usr/bin/ruby /usr/local/bin/ruby -s
fi

echo "Phase 2.2: Install extra packages..."
echo ""
cd $PACKAGE_ROOT/extra

mkdir -p /usr/local/bin
cp packet_worker_runner /usr/local/bin

yes A | unzip -q rubygems-1.3.5.zip
ruby rubygems-1.3.5/setup.rb --no-rdoc --no-ri
rm -r rubygems-1.3.5

echo "Phase 2.3: Install .gem packages..."
echo ""
cd $PACKAGE_ROOT/gems
gem install --no-ri --no-rdoc -l *.gem

if [[ $SINGLE_NODE == false ]]; then
  clear
  echo "Phase 3: Configure the Nova system"
  rake system:config

  clear
  echo "Phase 4: Install the Nova system"
  rake system:install

else
  echo "Phase 3: Configure a single node"
  rake config
fi

