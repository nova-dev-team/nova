#! /bin/sh


KEY_SOURCE=/keys
KEY_ADD_SCRIPT=${KEY_SOURCE}/addkey.sh
NOTE=`date "+%Y%m%d%H%M"`

mkdir $NOTE
expect ssh.exp
mv id_rsa* ${NOTE}

${KEY_ADD_SCRIPT} ssh-nopass 1 ${NOTE}
rm -r ${NOTE}

