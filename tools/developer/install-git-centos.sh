#!/bin/bash

# This script installs git (version 1.7.1.1) on CentOS distribution. It requires Internet connection.
#
# Author::    Santa Zhang (santa1987@gmail.com)

# Check if is CentOS distribution
if grep -q "CentOS" "/etc/issue" ; then
  echo "Current Linux distribution is CentOS"
else
  echo "This script only works on CentOS!"
  echo "For Ubuntu distribution, try 'apt-get install git-core'."
  exit 1
fi

yum makecache
yum groupinstall "Development Tools"
yum install -y gettext-devel expat-devel curl-devel zlib-devel openssl-devel

mkdir -p data

cd data
if [ -e "git-1.7.1.1" ]
then
  echo "git-1.7.1.1 already exists, skip downloading"
else
  wget http://kernel.org/pub/software/scm/git/git-1.7.1.1.tar.gz
  tar zxf git-1.7.1.1.tar.gz
fi
cd git-1.7.1.1

# configure git to be installed at /usr/bin/git
./configure --prefix=/usr
make all
make install

cd ../..

