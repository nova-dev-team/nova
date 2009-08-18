#! /bin/sh

APP_PATH=$1
ENTRY=$2
NODENAME=$3

cd ${APP_PATH}
/bin/sh ${ENTRY} ${NODENAME}

