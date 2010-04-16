# System settings.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class Setting < ActiveRecord::Base

  def Setting.vm_pool_size
    return (Setting.find_by_key "vm_pool_size").value.to_i
  end

  def Setting.storage_server
    return (Setting.find_by_key "storage_server").value
  end

  # Return the max VM use count. If a VM is used more then this count value, it will be destroyed.
  #
  # Since::   0.3
  def Setting.vm_max_use_count
    (Setting.find_by_key "vm_max_use_count").value
  end
end

