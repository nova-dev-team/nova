#! /bin/sh

NODELIST=config/node.list
SSH_PATH=/root/.ssh
AUTHORIZED_KEYS=${SSH_PATH}/authorized_keys

HOSTNAME=$1
cp ${HOSTNAME}/* ${SSH_PATH}

while read IP NNAME
do
	cat ${NNAME}/id_rsa.pub >> $AUTHORIZED_KEYS
done < $NODELIST

