#!/bin/sh

cd debs/archives
dpkg -i *.deb

cd ../..

cd gems
gem install --no-ri --no-rdoc -l *.gem
mkdir -p /usr/local/bin
cp packet_worker_runner /usr/local/bin

cd ..

rm -f /usr/bin/rake
rm -f /usr/bin/rails
ln /var/lib/gems/1.8/bin/rake /usr/bin/rake -s
ln /var/lib/gems/1.8/bin/rails /usr/bin/rails -s
ln /usr/bin/ruby /usr/local/bin/ruby -s
