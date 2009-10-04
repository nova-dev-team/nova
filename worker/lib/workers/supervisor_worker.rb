require "fileutils"
require "get_resource"
require "vmachines_helper"

class SupervisorWorker < BackgrounDRb::MetaWorker
  set_worker_name :supervisor_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  # remove stale .supervise files & stale vmachine directory
  def remove_stale_files    

    # remove stale vmachines directory
    Dir.entries(Setting.vmachines_root).each do |entry|
      next if entry.start_with? "." # skip hidden files
      entry_fullpath = "#{Setting.vmachines_root}/#{entry}"
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
    # TODO 
  end
end

