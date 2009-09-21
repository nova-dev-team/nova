#! /bin/sh

IFNAME="eth0"
BRNAME="br0"

IF_CONF_FILE="/etc/network/interfaces"

NOTE=`date +"%Y-%m-%d-%H-%M-%S"`

cp ${IF_CONF_FILE} ${IF_CONF_FILE}.backup.${NOTE}

sed -e "/auto ${BRNAME}/, /^\s*$/d" -i ${IF_CONF_FILE}

CONTEXT=`sed --quiet -e "/iface ${IFNAME}\s/,/^\s*$/p" ${IF_CONF_FILE} | sed -e "s/\s${IFNAME}\s/ ${BRNAME} /"`

DATE=`date`
echo ""				 >> ${IF_CONF_FILE}
echo "auto ${BRNAME}" 		 >> ${IF_CONF_FILE}
echo "${CONTEXT}" 		 >> ${IF_CONF_FILE}
echo "bridge_ports ${IFNAME}" 	 >> ${IF_CONF_FILE}
echo "# ${DATE}" 		 >> ${IF_CONF_FILE}
echo "" 			 >> ${IF_CONF_FILE}
exit 0

