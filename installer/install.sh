#!/bin/sh

cd debs
dpkg -i *.deb

cd ..

cd gems
gem install --no-ri --no-rdoc -l *.gem

rm /usr/bin/rake
rm /usr/bin/rails
ln /var/lib/gems/1.8/bin/rake /usr/bin/rake -s
ln /var/lib/gems/1.8/bin/rails /usr/bin/rails -s
