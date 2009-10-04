require "fileutils"
require "vmachines_helper"
require "image_resource"

class SupervisorWorker < BackgrounDRb::MetaWorker
  set_worker_name :supervisor_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  # remove stale .supervise files & stale vmachine directory
  def remove_stale_files
    # make sure the dir exists
    vmachines_root = Setting.vmachines_root
    FileUtils.mkdir_p vmachines_root

    # remove stale vmachines directory
    Dir.entries(vmachines_root).each do |entry|
      next if entry.start_with? "." # skip hidden files
      entry_fullpath = "#{vmachines_root}/#{entry}"
      logger.info "[supervisor_worker.remove_stale_files] rm_rf #{entry_fullpath}"
      FileUtils.rm_rf entry_fullpath unless Vmachine.all_names.include? entry
    end
  end

  # check if the (possible) copying process is working correctly
  def check_copying
    # TODO
  end

  # use md5sum to check if the image is corrupted
  def check_md5sum
    # TODO
  end

  # check if there is new file on server
  def update_server_image_list
    resource_list_cache_filename = Setting.resource_list_cache
    resource_list_cache_dirname = File.dirname resource_list_cache_filename
    FileUtils.mkdir_p resource_list_cache_dirname # make sure the dir exists

    resource_list = ImageResource.list_resource Setting.storage_server_vdisks
    resource_list_doc = resource_list.join "\n"
    File.open(resource_list_cache_filename, "w") do |file|
      file.write resource_list_doc
    end
  end
end

