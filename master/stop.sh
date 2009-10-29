#!/bin/sh

script/backgroundrb stop

if [ -e "tmp/pids/server.pid" ]
then
  read hg< "tmp/pids/server.pid"
  kill ${hg}
fi

killall mongrel
killall swiftiply
killall swiftiply_mongrel_rails
killall mongrel_rails
rm -rf log/*.pid
