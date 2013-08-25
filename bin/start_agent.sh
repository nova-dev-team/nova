#! /bin/sh
sh ln_lib.sh
export LD_LIBRARY_PATH=../lib/sigar:../lib/:$LD_LIBRARY_PATH
java -jar Nova_Agent.jar
