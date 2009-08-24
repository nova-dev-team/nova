require 'rubygems'
require 'libvirt'
require 'fileutils'
require 'ftools'
require 'pp'
require 'uri'
require 'net/scp'

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
# 
# UPDATED (2009-08-16):
#
# * The system will use qcow2 format disk, so raw image files will NOT be used.
#
 

class VmachinesWorker < BackgrounDRb::MetaWorker
  
  @@virt_conn = VmachinesHelper::Helper.virt_conn

  set_worker_name :vmachines_worker

  def create(args = nil)
    # this method is called, when worker is loaded for the first time
  end

  # setup status, copy files, then start vm
  def do_start params

    # updating 'progress' percentage is handled by scheduled supervisor
    progress = Progress.new
    progress[:owner] = params[:name]
    begin
      dom = @@virt_conn.lookup_domain_by_uuid params[:uuid]
      vmachine_dir = "#{params[:vmachines_root]}/#{params[:name]}"

      if params[:cdrom]
        progress[:info] = "downloading cdrom image: #{params[:cdrom]}"
        progress[:status] = "in progress"
        progress.save

        local_filename = request_resource "#{params[:storage_server]}/#{params[:cdrom]}", params[:storage_cache]
        FileUtils.ln_s local_filename, "#{vmachine_dir}/#{params[:cdrom]}"
      end

      if params[:hda]
        progress[:info] = "downloading hda image: #{params[:hda]}"
        progress[:status] = "in progress"
        progress.save

        local_filename = request_resource "#{params[:storage_server]}/#{params[:hda]}", params[:storage_cache]
        FileUtils.ln_s local_filename, "#{vmachine_dir}/base.#{params[:hda]}"
        qcow2_cmd = "qemu-img create -b #{vmachine_dir}/base.#{params[:hda]} -f qcow2 #{vmachine_dir}/#{params[:hda]}"
        logger.debug "*** [cmd] #{qcow2_cmd}"
        `#{qcow2_cmd}`
      end

      progress[:info] = "starting vmachine '#{params[:name]}'"
      progress[:status] = "in progress"
      progress.save

      dom.create

      # delete the progress information, 'cause its no longer useful
      Progress.delete progress

    rescue Exception => e
      progress[:info] = "error on server side, check log files"
      progress[:status] = "error occured"
      progress.save

      # log backtarce to file
      logger.debug e.pretty_inspect.to_s + "\n" + e.backtrace.pretty_inspect.to_s
    end
  end

  # cleanup after a vmachine is destroyed
  # TODO upload image files onto storage server
  def do_cleanup args
    begin
      logger.debug "*** [remove] #{args[:vmachines_root]}/#{args[:vmachine_name]}"

      # remove dirty progress information, if exists (ususlly when the machine failed to start)
      progress = Progress.find_by_owner args[:vmachine_name]
      Progress.delete progress if progress

      # remove local resource
      FileUtils.rm_rf "#{args[:vmachines_root]}/#{args[:vmachine_name]}"
    rescue Exception => e
      logger.debug e.pretty_inspect.to_s + "\n" + e.backtrace.pretty_inspect.to_s
    end
  end

private

  # returns the full path of cached file, and the file will be locked as "in use"
  def request_resource resource_uri, cache_root
    FileUtils.mkdir_p cache_root  # assure existance of cache root dir

    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split resource_uri  # parse URI information
    resource_filename = File.basename path
    local_to = "#{cache_root}/#{resource_filename}"
    copying_lock_filename = local_to + ".copying"

    if File.exist? local_to # has local file, probably under copying
      sleep 1 while File.exist? copying_lock_filename # while if is under copying
    else  # file does not exit, should copy
      copying_lock = File.new(copying_lock_filename, "w")
      copying_lock.flock File::LOCK_EX

      get_file resource_uri, local_to

      copying_lock.flock File::LOCK_UN
      copying_lock.close
      FileUtils.rm copying_lock_filename
    end
    return local_to
  end

  # get a file from some uri, and save to a file
  #
  # assumption: from_uri accessable, in schemes of "ftp, scp, file, carrierfs?"
  #             to_file does not exist
  def get_file from_uri, to_file
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split from_uri  # parse URI information
  
    logger.debug "Retrieving file from: #{from_uri}"

    FileUtils.mkdir_p(File.dirname to_file)  # assure existance of file directory
    if scheme == "file"
      FileUtils.cp path, to_file
    elsif scheme == "ftp"
    elsif scheme == "scp"
      sep_index = userinfo.index ":"
      username = userinfo[0...sep_index]  # notice, ... rather than ..
      password = userinfo[(sep_index + 1)..-1]
      Net::SCP.download!(host, username, path, to_file, :password => password)
    else
      raise "Resource scheme '#{scheme}' not known!"
    end
  end

  # TODO upload 'from_file' -> 'to_uri'
  def put_file from_file, to_uri
  end

  # called every a few seconds, check if local files are in good health
  def VmachinesWorker.supervise
    `touch good`
  end

end

