#!/bin/sh

LIST_FILE=./debs.list
DEBS_DIR=debs
DEBS_DIR_FULL_PATH=$(readlink -f $DEBS_DIR)

mkdir -p $DEBS_DIR_FULL_PATH/archives/partial

for deb_package in $(cat $LIST_FILE)
do
  echo "*** Fetching" $deb_package
  yes | apt-get install --reinstall -d -o dir::cache=$DEBS_DIR_FULL_PATH $deb_package
done

