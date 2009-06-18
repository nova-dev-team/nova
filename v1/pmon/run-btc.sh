#! /bin/sh

cd btc.run/level-1-controller/
script/server &
cd -
cd btc.run/level-1-se/src/
./newsedaemon.rb &
cd -
cd btc.run/level-1-ue/src/
./newuedaemon.rb &
cd -
