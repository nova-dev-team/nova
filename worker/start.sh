#!/bin/sh

echo Backgroundrb is set to 'development' environment
echo To change it, edit 'start.sh'
echo Or you can run it by command:
echo "  script/backgroundrb start -e development"
echo
echo "*** NOTICE: Please make sure you have run 'first_run.sh' once"
echo

script/backgroundrb start -e development
script/server $@ -d
