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

  @@NOVA_ROOT = nil # cached for readonly value
  # Return the source code directory.
  #
  # Since::   0.3
  def Setting.nova_root
    return @@NOVA_ROOT if @@NOVA_ROOT
    @@NOVA_ROOT = (Setting.find_by_key "nova_root").value
  end

  # Return the size of image pool.
  #
  # Since::   0.3
  def Setting.image_pool_size
    (Setting.find_by_key "image_pool_size").value.to_i
  end

  # Return the storage server's address. For version 0.3, it is an FTP site.
  # The URI should look like: ftp://user:password@somewhere, there is no '/' at the end.
  #
  # Since::   0.3
  def Setting.storage_server
    (Setting.find_by_key "storage_server").value
  end

end

