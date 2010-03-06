class Setting < ActiveRecord::Base

  @@RUN_ROOT = nil  # cached for readonly value
  def Setting.run_root
    return @@RUN_ROOT if @@RUN_ROOT
    @@RUN_ROOT = (Setting.find_by_key "run_root").value
  end

  # where all the image files are located
  def Setting.storage_server_vdisks
    (Setting.find_by_key "storage_server").value + "/vdisks"
  end

  # where should the cached files be stored
  @@STORAGE_CACHE = nil # cached for readonly value
  def Setting.storage_cache
    return @@STORAGE_CACHE if @@STORAGE_CACHE
    @@STORAGE_CACHE = (Setting.find_by_key "storage_cache").value
  end

  # where is the root hosting vmachines
  @@VMACHINES_ROOT = nil # cache for readonly value
  def Setting.vmachines_root
    return @@VMACHINES_ROOT if @@VMACHINES_ROOT
    @@VMACHINES_ROOT = (Setting.find_by_key "vmachines_root").value
  end

  # the file to cache resource listing on storage server
  @@RESOURCE_LIST_CACHE = nil # cache for readonly value
  def Setting.resource_list_cache
    return @@RESOURCE_LIST_CACHE if @@RESOURCE_LIST_CACHE
    @@RESOURCE_LIST_CACHE = (Setting.find_by_key "resource_list_cache").value
  end

  # whether we should do image polling
  def Setting.image_pooling?
    (Setting.find_by_key "image_pooling").value == "true"
  end

  def Setting.image_pooling_count
    (Setting.find_by_key "image_pooling_count").value.to_i
  end

end

