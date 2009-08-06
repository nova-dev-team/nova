#!/bin/sh

cd debs
dpkg -i *.deb

cd ..

cd gems
gem install --no-ri --no-rdoc -l *.gem

