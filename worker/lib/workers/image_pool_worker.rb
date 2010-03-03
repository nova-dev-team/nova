require "image_resource"

class ImagePoolWorker < BackgrounDRb::MetaWorker
  set_worker_name :image_pool_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  def ensure_image_pool_size
    if Setting.image_pooling?
      Dir.entries(Setting.storage_cache).each do |entry|
        break unless Setting.image_pooling? # stop pooling as soon as possible, if required
        next unless entry.end_with? ".qcow2" # only takes care of qcow2 images
        entry_fullpath = "#{Setting.storage_cache}/#{entry}"
        next if File.exist? "#{entry_fullpath}.copying" # skip files if they are under copying (being downloaded)
        (1..Setting.image_pooling_count).each do |id|
          break unless Setting.image_pooling? # stop pooling as soon as possible, if required
          ImageResource.copy_pool_image "#{Setting.storage_cache}/#{entry}", id
        end 
      end
    end
  end

end

