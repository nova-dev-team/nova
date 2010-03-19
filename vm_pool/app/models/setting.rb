# System settings.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class Setting < ActiveRecord::Base

  @@VM_POOL_SIZE = nil
  def Setting.vm_pool_size
    return @@VM_POOL_SIZE if @@VM_POOL_SIZE
    @@VM_POOL_SIZE = (Setting.find_by_key "vm_pool_size").value.to_i
  end

  @@STORAGE_SERVER = nil
  def Setting.storage_server
    return @@STORAGE_SERVER if @@STORAGE_SERVER
    @@STORAGE_SERVER = (Setting.find_by_key "storage_server").value
  end

end

