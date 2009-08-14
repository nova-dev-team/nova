#!/bin/sh

script/backgroundrb stop

if [ -e "tmp/pids/server.pid" ]
then
  read hg< "tmp/pids/server.pid"
  kill ${hg}
fi

