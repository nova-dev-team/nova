#! /bin/sh

SERVER_KEY_STORE_PATH="/keys"

if [ $# -ne 3 ] 
then
	echo "Usage: addkey package_name credit_of_key key_source"
	exit 1
fi

PACKAGE_NAME=$1
REMAIN=$2
KEYSOURCE=$3

KEY_AVAILABLE_DIR="${SERVER_KEY_STORE_PATH}/${PACKAGE_NAME}/available"
LOCK="${SERVER_KEY_STORE_PATH}/${PACKAGE_NAME}/lock"

BIGGEST=`ls ${KEY_AVAILABLE_DIR}/ | sort -nr | head -1`
KEY_ID=`expr ${BIGGEST} + 1`

KEY_PATH="${KEY_AVAILABLE_DIR}/${KEY_ID}"
KEY_CONTENT_PATH="${KEY_PATH}/key"

mkdir $KEY_PATH
mkdir $KEY_CONTENT_PATH

touch ${KEY_PATH}/history
echo $REMAIN >> ${KEY_PATH}/remain
cp -r $KEYSOURCE/* $KEY_CONTENT_PATH

echo "New key has been added to ${KEY_AVAILABLE_DIR}, ID=${KEY_ID}, Remains ${REMAIN}"

