# The model for master's settings
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'fileutils'

class Setting < ActiveRecord::Base

  # Intercepts savings for "storage_server", and updates corresponding configs.
  #
  # Since::   0.3
  def save
    if self.key == "storage_server"
      # update also write to config/storage_server.conf
      File.open("#{RAILS_ROOT}/config/storage_server.conf", "w") do |f|
        f.write self.value
      end

      # update also write to run_root
      FileUtils.mkdir_p Setting.run_root
      File.open("#{Setting.run_root}/ftp_server_files_list_updater_lftp_script", "w") do |f|
        f.write <<LFTP_SCRIPT
set net:timeout 10
set net:max-retries 2
set net:reconnect-interval-base 1
open #{self.value}
cd /vdisks
pwd
ls
cd /agent_packages
pwd
ls
LFTP_SCRIPT
      end

      puts "Update to storage_server also forwarded to config/storage_server.conf!"
    end
    super
  end


  @@SYSTEM_ROOT = nil # cached for readonly value
  # Return the source code directory.
  #
  # Since::   0.3
  def Setting.system_root
    return @@SYSTEM_ROOT if @@SYSTEM_ROOT
    @@SYSTEM_ROOT = (Setting.find_by_key "system_root").value
  end

  # Return the storage server's address. For version 0.3, it is an FTP site.
  # The URI should look like: ftp://user:passwd@somewhere.com/, the trailing '/' is optional.
  #
  # Since::   0.3
  def Setting.storage_server
    (Setting.find_by_key "storage_server").value
  end

  @@RUN_ROOT = nil  # cached for readonly value
  # Return the working directory for worker module.
  #
  # Since::   0.3
  def Setting.run_root
    return @@RUN_ROOT if @@RUN_ROOT
    @@RUN_ROOT = (Setting.find_by_key "run_root").value
  end

  # Get all settings that are intended for worker
  #
  # Since::   0.3
  def Setting.all_for_worker
    Setting.find_all_by_for_worker true
  end

  # Get the first ip address for VMs.
  #
  # Since::   0.3
  def Setting.vm_first_ip
    (Setting.find_by_key "vm_first_ip").value
  end

  # Get the gateway for the VMs.
  #
  # Since::   0.3
  def Setting.vm_gateway
    (Setting.find_by_key "vm_gateway").value
  end

  # Get the subnet mask for the VMs.
  #
  # Since::   0.3
  def Setting.vm_subnet_mask
    (Setting.find_by_key "vm_subnet_mask").value
  end

end

