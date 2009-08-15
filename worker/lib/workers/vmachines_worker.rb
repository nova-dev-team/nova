require 'libvirt'
require 'fileutils'
require 'ftools'
require 'pp'
require 'uri'

# About the resource caching policy
#
# 1 Caching convention
#
#   Files are cached to "storage_cache" folder (params[:storage_cache]).
#   Copy-on-Write and readonly files only need to have one copy, but raw
#   files could have multiple copies. We appand an random number as
#   suffix to the resource filename to prevent overwriting. So, put it
#   in example:
#
#   Original files on image server:
#
#     i1-sys-ubuntu_904.raw (raw file)
#     i2-usr-winxp_sp3.qcow2 (copy-on-write)
#     i3-usr-winxp_sp3_setup.iso (readonly)
#   
#   Cached:
#
#     i1-sys-ubuntu_904.raw.23234234 (cache1)
#     i1-sys-ubuntu_904.raw.1212312312 (cache2)
#     i1-sys-ubuntu_904.raw.13123124 (cache3)
#     i2-usr-winxp_sp3.qcow2 (copy-on-write, only 1 copy required)
#     i3-usr-winxp_sp3_setup.iso (readonly, only 1 copy required)
#
#
# 2 Caching status, locking of image files
#
#   Image files need to be copied from server, in the mean time it cannot
#   be used (image not ready). We need to add a few "status flags" to the images.
#   Because we are not using database to store the status flags, we would
#   store the status flags as files.
#
#   For every image, we need to know if it is ready, or is under copying process.
#   If an image file named "F" is being copied, we will create a new empty file
#   named "F.copying" to mark "F" as "under copying process".
#
#   For copy-on-write and readonly resource, we need to know which vmachines are
#   using them. If it is still under use, it cannot be removed (to make room
#   for more frequently used images). So if "F" is under use by vmachine "vm(name)", 
#   an file "F.using.by.vm(name)" is created.
#
#   For raw disks, when they are copied, they are either directly moved to vmachines folder,
#   or remain to be used in the cache direcotry. If a file "F.(random_suffix).taken.by.vm(name)" exists,
#   it means the file under copying "F.(random_suffix)" is already taken, and will be used when
#   finished copying. Else if the ".taken.by.vm(name)" file does not exist, it means this files
#   could be used by new vmachines.
#
# 3 After vmachines are destroyed
#
#   If the image file is "user-image", it is sent back to image server.
#   Else if the image file is "system-image", and the user has requested it to be changed
#   as user-image, it will also be uploaded, and change to "user-image".
#   Else (the image is "system-image", and not to be changed as "user-image"), the image
#   is directly discarded.
#
#   F.save.to.uid.(uid)
#   F.comment <- text file containing comments about the image file
#
 

class VmachinesWorker < BackgrounDRb::MetaWorker
  
  @@virt_conn = Libvirt::open("qemu:///system")

  set_worker_name :vmachines_worker
  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  # setup status, copy files, then start vm
  def do_start params
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]

      if params[:cdrom]
        local_filename = request_resource "#{params[:storage_server]}/#{params[:cdrom]}", params[:storage_cache]
        FileUtils.ln_s local_filename, "#{params[:vmachines_root]}/#{params[:name]}/#{params[:cdrom]}"
      end

      if params[:hda]
        local_filename = request_resource "#{params[:storage_server]}/#{params[:hda]}", params[:storage_cache]
        if resource_copy_on_write? local_filename
          # TODO deal with copy on write images
          FileUtils.cp local_filename, "#{params[:vmachines_root]}/#{params[:name]}/#{params[:hda]}"
        else  # not copy on write
          FileUtils.mv local_filename, "#{params[:vmachines_root]}/#{params[:name]}/#{params[:hda]}"
        end
      end

      dom.create
    rescue Exception => e
      # TODO report error by setting status
    
      # log backtarce to file
      log e.pretty_inspect.to_s + "\n" + e.backtrace.pretty_inspect.to_s
    end
  end


private

  # TODO check if resource already in cache?
  def already_cached? resource_uri, cache_root
    false
  end

  # TODO make sure resource is cached
  # returns the full path of cached file, and the file will be locked as "in use"
  def request_resource resource_uri, cache_root
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split resource_uri  # parse URI information
    FileUtils.mkdir_p cache_root  # assure existance of cache root dir

    # TODO first, check if has local resource

    resource_filename = resource_uri[(resource_uri.rindex '/') + 1..-1]
    if scheme == "file" # local file copy
      local_from = path

      if resource_readonly? resource_filename or resource_copy_on_write? resource_filename
        # read only images/copy on write, only one copy, no need to suffix rand number
        local_to = "#{cache_root}/#{resource_filename}"
      else
        local_to = "#{cache_root}/#{resource_filename}.#{rand.to_s[2..-1]}" # suffix by rand number, so one resource could have multiple copies
      end

      if File.exist? local_to # image exists, test if it is ready
        sleep 1 while File.exist? local_to + ".copying" # wait 1 sec then check
      else # image does not exist, so do copying!
        copying_lock_filename = local_to + ".copying"
        copying_lock = File.new(copying_lock_filename, "w")
        copying_lock.flock(File::LOCK_EX)
        FileUtils.cp local_from, local_to
        copying_lock.flock(File::LOCK_UN)
        copying_lock.close

        log "remving #{copying_lock_filename}"
        FileUtils.rm copying_lock_filename
      end

      return local_to
    elsif scheme == "ftp"
      # TODO get image by ftp
    elsif scheme == "scp"
      # TODO get image by scp
    elsif scheme == "http"
      # TODO get image by http
    end
  end

  # TODO try to lock resources
  # local_resource_filename does not have full path
  def lock_resource local_resoure_filename
    return false # return false: lock failed, should download new file
  end
  
  # TODO check if the resource is read only (eg. cdrom-iso)
  def resource_readonly? resource_uri
    resource_uri.downcase.end_with? ".iso"
  end

  def resource_copy_on_write? resource_uri
    resource_uri.downcase.end_with? ".qcow2"
  end

  def log msg
      File.open("#{RAILS_ROOT}/log/vmachines.worker.err", "a") {|f| f.write(Time.now.to_s + "\n" + msg.to_s + "\n\n")}
  end

end

