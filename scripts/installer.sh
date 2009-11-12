#!/bin/bash

# must run by root
if [[ $UID -ne 0 ]]; then
  echo "This script requires root privilege!"
  exit 1
fi

# only supports ubuntu
# TODO check distributation

clear

echo ""
echo "This script installs the Nova platform."
echo "However, the platform will not be fully configured by this script."
echo "You need to manually do configuration work."
echo ""

read -p "Press any key to start installation..."

# phase 1: download packages

clear
echo "Phase 1: Download .deb packages..."
echo ""

DEBS_LIST_FILE=$(readlink -f ./config/installer/debs.list)
PACKAGE_ROOT=$(readlink -f ./data/installer)
DEBS_PACKAGE_DIR=$PACKAGE_ROOT/debs

mkdir -p $DEBS_PACKAGE_DIR/archives/partial
all_debs=( $( cat $DEBS_LIST_FILE ) )
(yes | apt-get install --reinstall -d -o dir::cache=$DEBS_PACKAGE_DIR ${all_debs[@]}) || exit 1

# Phase 2: begin installing process

clear
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

clear
echo "Phase 3: Configure the Nova system"
rake system:install
