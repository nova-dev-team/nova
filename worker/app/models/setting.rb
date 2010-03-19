# The model for worker's saved settings.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'fileutils'

class Setting < ActiveRecord::Base

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
    elsif self.key == "image_pool_size"
      begin
        image_pool_maintainer_conf = File.read "#{RAILS_ROOT}/lib/image_pool_maintainer.conf"
      rescue
        # when config file is not ready, just create a new file for it
        File.open("#{RAILS_ROOT}/lib/image_pool_maintainer.conf", "w") do |f|
          f.write "pool_size=#{self.value}\n"
        end
        retry
      end
      File.open("#{RAILS_ROOT}/lib/image_pool_maintainer.conf", "w") do |f|
        image_pool_maintainer_conf.each_line do |line|
          line = line.strip
          if line.start_with? "pool_size="
            f.write "pool_size=#{self.value}\n"
          else
            f.write "#{line}\n"
          end
        end
      end
    end
    super
  end

  @@RUN_ROOT = nil  # cached for readonly value
  # Return the working directory for worker module.
  #
  # Since::   0.3
  def Setting.run_root
    return @@RUN_ROOT if @@RUN_ROOT
    @@RUN_ROOT = (Setting.find_by_key "run_root").value
  end

  # Return the running directory for virtual machines.
  #
  # Since::     0.3
  def Setting.vm_root
    return File.join Setting.run_root, "vm"
  end

  # Return the image pool root directory.
  #
  # Since::     0.3
  def Setting.image_pool_root
    return File.join Setting.run_root, "image_pool"
  end

  @@SYSTEM_ROOT = nil # cached for readonly value
  # Return the source code directory.
  #
  # Since::   0.3
  def Setting.system_root
    return @@SYSTEM_ROOT if @@SYSTEM_ROOT
    @@SYSTEM_ROOT = (Setting.find_by_key "system_root").value
  end

  # Return the size of image pool.
  #
  # Since::   0.3
  def Setting.image_pool_size
    (Setting.find_by_key "image_pool_size").value.to_i
  end

  # Return the storage server's address. For version 0.3, it is an FTP site.
  # The URI should look like: ftp://user:passwd@somewhere.com/, the trailing '/' is optional.
  #
  # Since::   0.3
  def Setting.storage_server
    (Setting.find_by_key "storage_server").value
  end

end

