#!/bin/sh

if [ "$(id -u)" != "0" ]; then
	echo "This script must be run as root!" 1>&2
	exit 1
fi

cd debs/archives
dpkg -i *.deb

cd ../..

cd more
mkdir -p /usr/local/bin
cp packet_worker_runner /usr/local/bin

yes A | unzip -q rubygems-1.3.5.zip
cd rubygems-1.3.5
ruby setup.rb --no-rdoc --no-ri
cd ..
rm -r rubygems-1.3.5

# install swiftiply
cd swiftiply-0.6.1.1
ruby setup.rb
cd ..

cd ..

cd gems
gem install --no-ri --no-rdoc -l *.gem
cd ..

if [ ! -e /usr/local/bin/ruby ]
then
	ln /usr/bin/ruby /usr/local/bin/ruby -s
fi

