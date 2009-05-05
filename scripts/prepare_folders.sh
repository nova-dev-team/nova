#!/bin/bash

mkdir -p /config
mkdir -p /share

read NFSSERVER < nfsserver.conf
mount ${NFSSERVER}:/config /config
mount ${NFSSERVER}:/share /share


