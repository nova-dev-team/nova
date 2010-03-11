# The model for worker's saved settings.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

class Setting < ActiveRecord::Base

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
  # The URI should look like: ftp://user:password@somewhere/, the trailing '/' is optional.
  #
  # Since::   0.3
  def Setting.storage_server
    (Setting.find_by_key "storage_server").value
  end

end

