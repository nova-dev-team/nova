#!/bin/bash

LIST_FILE=./debs.list
DEBS_DIR=debs
DEBS_DIR_FULL_PATH=$(readlink -f $DEBS_DIR)

mkdir -p $DEBS_DIR_FULL_PATH/archives/partial

all_debs=( $( cat debs.list ) )

yes | apt-get install --reinstall -d -o dir::cache=$DEBS_DIR_FULL_PATH ${all_debs[@]}

