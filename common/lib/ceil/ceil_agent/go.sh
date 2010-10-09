#!/bin/sh

mv history/1 queue/1
make clean
make
./ceil_executor
