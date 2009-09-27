require 'vmachines_worker'


# this is a helper worker for vmachines_worker, actually, for its "update" method.
# it takes care of image pooling, make sure that there is enough images being pooled.
class UpdateWorker < BackgrounDRb::MetaWorker
  set_worker_name :update_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  def do_pooling
    logger.debug "[updater] pooling"
    if Setting.image_polling?
      Dir.entries(Setting.storage_cache).each do |entry|
        next unless entry.end_with? ".qcow2" # only takes care of qcow2 images
        entry_fullpath = "#{Setting.storage_cache}/#{entry}"
        next if File.exist? "#{entry_fullpath}.copying" # skip files if they are under copying
        (1..Setting.image_polling_count).each do |id|
          VmachinesWorker::copy_pooling_image "#{Setting.storage_cache}/#{entry}", id
        end 
      end
    end
  end

end

