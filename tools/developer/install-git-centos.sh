#!/bin/bash

# This script installs git (version 1.7.1.1) on CentOS distribution. It requires Internet connection.
#
# Author::    Santa Zhang (santa1987@gmail.com)

yum makecache
yum groupinstall "Development Tools"
yum install gettext-devel expat-devel curl-devel zlib-devel openssl-devel

cd data
wget http://kernel.org/pub/software/scm/git/git-1.7.1.1.tar.gz
tar zxf git-1.7.1.1.tar.gz
cd git-1.7.1.1

# configure git to be installed at /usr/bin/git
./configure --prefix=/usr
make all
make install

cd ../..

