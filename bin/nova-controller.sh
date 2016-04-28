#!/usr/bin/bash

# @author Tianyu Chen
# This script fires up and controls the entire Nova system. 
# It depends on pgrep and parallel-ssh. 
# Pgrep is installed by default in a recent release of CentOS/RHEL
# if you make a full installation. 
# To install parallel-ssh you first enable the EPEL repository and
# install it via the yum package manager. 

# Set ips of slave nodes here. 
HOSTS_FILE="slaves"

# Colored output
RED=`tput setaf 1`
GREEN=`tput setaf 2`
BOLD=`tput bold`
RESET=`tput sgr 0`

function start_nova {
    # start slaves
    pssh -l root -p 1 -i -t 3 --hosts "$HOSTS_FILE" "cd /home/Nova; nohup ./start_worker.sh &" > /dev/null
    # start master
    nohup ./start_master.sh &
}

function stop_nova {
    # stop slaves
    pssh -l root -p 1 -i -t 3 --hosts "$HOSTS_FILE" "killall -9 java"
    # stop master
    killall -9 java
}

function nova_status {
    # show slaves status
    # pssh -l root -p 1 -i -t 3 --hosts "$HOSTS_FILE" "pgrep java" 
    cat slaves | while read hostname; do
        err_cnt=`pssh -l root -p 1 -i -t 3 --host "$hostname" "pgrep java" | grep "FAILURE" -c`
        if [ $err_cnt -eq 0 ]; then
            echo "Java is ${GREEN}${BOLD}[running]${RESET} on ${hostname}. "
        else
            echo "Java has ${RED}${BOLD}[stopped]${RESET} on ${hostname}. "
        fi
    done
    # show master status
    if [ "`pgrep java`" == "" ]; then
        echo "Java has ${RED}${BOLD}[stopped]${RESET} on localhost(master). "
    else
        echo "Java is ${GREEN}${BOLD}[running]${RESET} on localhost(master). "
    fi
}

if [ "$1" == "start" ]; then
    echo "Starting Nova..."
    start_nova
    nova_status
elif [ "$1" == "stop" ]; then
    echo "Stopping Nova..."
    stop_nova
    nova_status
elif [ "$1" == "restart" ]; then
    echo "Restarting..."
    stop_nova
    sleep 2
    nova_status
    start_nova
    nova_status
elif [ "$1" == "status" ]; then
    nova_status
else
    echo "Usage: $0 start|stop|restart|status"
fi

