#!/bin/bash

id=$1
node=node${id}
scp create_br.sh ${node}:/tmp/

ssh ${node} /tmp/create_br.sh
ssh ${node} /etc/init.d/networking restart &

echo "${node} 's br0 has been configured."
