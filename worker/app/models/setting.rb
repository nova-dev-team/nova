class Setting < ActiveRecord::Base

  # where all the image files are located
  def Setting.storage_server
    (Setting.find_by_key "storage_server").value
  end

  # where should the cached files be stored
  def Setting.storage_cache
    (Setting.find_by_key "storage_cache").value
  end

  # where is the root hosting vmachines
  def Setting.vmachines_root
    (Setting.find_by_key "vmachines_root").value
  end

end

