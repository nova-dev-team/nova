#which nic of master handles ceil
CEIL_NETWORK_INTERFACE = "eth1"

#by default, package & key server is set on master
CEIL_PACKAGE_SERVER = `ifconfig #{CEIL_NETWORK_INTERFACE} | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;
CEIL_KEY_SERVER = `ifconfig #{CEIL_NETWORK_INTERFACE} | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;

#server type for client
CEIL_PACKAGE_SERVER_TYPE = "ftp"
CEIL_KEY_SERVER_TYPE = "ftp"

