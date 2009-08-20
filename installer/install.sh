#!/bin/sh

cd debs/archives
dpkg -i *.deb

cd ../..

cd more
mkdir -p /usr/local/bin
cp packet_worker_runner /usr/local/bin

yes A | unzip rubygems-1.3.5.zip
cd rubygems-1.3.5
ruby setup.rb --no-rdoc --no-ri
cd ..
rm -r rubygems-1.3.5

cd ..

cd gems
gem install --no-ri --no-rdoc -l *.gem
cd ..


if ! [ -e /usr/bin/rake ]
then
	ln /var/lib/gems/1.8/bin/rake /usr/bin/rake -s
fi

if ! [ -e /usr/bin/rails ]
then
	ln /var/lib/gems/1.8/bin/rails /usr/bin/rails -s
fi

if ! [ -e /usr/local/bin/ruby ]
then
	ln /usr/bin/ruby /usr/local/bin/ruby -s
fi

