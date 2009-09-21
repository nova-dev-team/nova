CEIL_ROOT = "#{RAILS_ROOT}/lib/ceil"

#which nic of master handles ceil
CEIL_NETWORK_INTERFACE = "eth1"

#by default, package & key server is set on master
#CEIL_PACKAGE_SERVER = `ifconfig #{CEIL_NETWORK_INTERFACE} | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;
#CEIL_KEY_SERVER = `ifconfig #{CEIL_NETWORK_INTERFACE} | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;

CEIL_PACKAGE_SERVER = "10.0.0.1"
CEIL_KEY_SERVER = "10.0.0.1"

#server type for client
CEIL_PACKAGE_SERVER_TYPE = "ftp"
CEIL_KEY_SERVER_TYPE = "ftp"


#client progress id
CEIL_STATUS = {
0 => "ceil started",
10 => "start downloading binary package",
15 => "downloading finished, package",
30 => "start installing binary package",
35 => "installing finished, package",
50 => "start downloading key for package",
55 => "downloading finished, key for package",
70 => "start dispatching key for package",
75 => "dispatching finished, key for package",
80 => "all done, package",
100 => "ceil exited"
}

#max app installation id 
#for calc progress
CEIL_APP_STATUS_MAX = 80
#steps to install an app
CEIL_APP_INSTALL_STEPS = 9   # et. 10 15 30 35 50 55 70 75 80

module CeilMessage
  def CeilMessage.message(status, category)
    id = status.abs
    msg = CEIL_STATUS[id] + " " + category
    msg = "Failed: " + msg if status < 0
    return msg
  end
end

