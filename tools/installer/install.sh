#!/bin/bash

clear
echo
echo "This script installs the Nova platform. Currently it only supports Ubuntu distribution."
echo
echo "The installation process::"
echo "1: Install depended .deb packages"
echo "2: Install depended .gem packages"
echo "3: Compiling used tools"
echo "4: Configure the Nova system"
echo "5: Create worker node installer"
echo
read -p "Press any key to start installation, or press Ctrl+C to quit..."

clear
echo "Phase 1: Installing depended .deb packages..."
echo

# where is the script?
SCRIPT_ROOT=$(dirname $0)
DEBS_LIST=$SCRIPT_ROOT/data/debs.list
DEBS_DIR=$SCRIPT_ROOT/data/debs

# make folder for apt-get
mkdir -p $DEBS_DIR/archives/partial
all_debs=( $( cat $DEBS_LIST ) )
sudo apt-get install -y ${all_debs[@]} || exit 1

# switch to ruby script
echo
ruby $SCRIPT_ROOT/install_stage2.rb

