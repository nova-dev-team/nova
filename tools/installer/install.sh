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
echo "This script installs the Nova platform. Currently it only supports Ubuntu distribution."
echo "You may want to modify '../../common/config/conf.yml' according to your needs."
echo
echo "The installation process:"
echo "1: Install depended .deb packages"
echo "2: Install depended .gem packages"
echo "3: Compile utility tools"
echo "4: Install Nova master module"
echo "5: Create worker node installer"
echo "6: Configure the Nova system"
echo "7: Install Nova worker module"
echo
echo "This script does steps 1~4 for you. You will have to do step 5~7 by hand. See README for more info."
echo
read -p "Press any key to start installation, or press Ctrl+C to quit..."

clear
echo "Phase 1: Installing depended .deb packages..."
echo

# where is the script?
SCRIPT_ROOT=$(dirname $0)
DEBS_LIST=$SCRIPT_ROOT/data/debs.list
DEBS_DIR=$SCRIPT_ROOT/data/debs

apt-get update
all_debs=( $( cat $DEBS_LIST ) )
apt-get install -y ${all_debs[@]} || exit 1

# switch to ruby script
echo
ruby $SCRIPT_ROOT/lib/install_stage2.rb

