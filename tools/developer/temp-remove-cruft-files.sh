#!/bin/bash

# remove xen-3.3.1.tar.gz & kernel source files

# go to source root
cd ../..

./tools/developer/git-remove-history.sh tools/installer/data/src/xen-3.3.1.tar.gz tools/installer/data/src/kernel-2.6.18-164.15.1.el5.src.rpm

