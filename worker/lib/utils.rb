# This file provides basic utility for worker module.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require "#{RAILS_ROOT}/../common/lib/utils.rb"


# Writes FTP address of the storage server to config/storage_server.conf.
#
# Since::     0.3
def write_storage_server_to_config storage_server
  File.open("#{RAILS_ROOT}/config/storage_server.conf", "w") do |f|
    f.write storage_server
  end
end

