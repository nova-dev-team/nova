#!/bin/sh

script/backgroundrb start -e development
script/server $@
