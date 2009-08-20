#!/bin/sh

cd debs/archives
dpkg -i *.deb

cd ../..

cd gems
gem install --no-ri --no-rdoc -l *.gem
cd ..

cd more
mkdir -p /usr/local/bin
cp packet_worker_runner /usr/local/bin

unzip rubygems-1.3.5.zip
cd rubygems-1.3.5
ruby setup.rb
cd ..

cd ..

rm -f /usr/bin/rake
rm -f /usr/bin/rails
ln /var/lib/gems/1.8/bin/rake /usr/bin/rake -s
ln /var/lib/gems/1.8/bin/rails /usr/bin/rails -s
ln /usr/bin/ruby /usr/local/bin/ruby -s
