require 'libvirt'
require 'fileutils'
require 'ftools'
require 'pp'
 

## resource caches format
#
#  raw file:                disk.img
#  cached file:             disk.img.2313  (suffixed an random number)
#  cached flag:             disk.img.2313.ready (means the disk file is ready to be used)
#  lock of cached file:     disk.img.2313.using (means it is already under use)
#  cache under copying:     disk.img.2313.copying (means it is under copy)
#
#  ** usually, after copying, an image will be directly changed to .using status 
#
#  for read only resource, it will not have random suffix, and will only have .copying or .ready status 
#


class VmstartWorker < BackgrounDRb::MetaWorker
  
  
  @@virt_conn = Libvirt::open("qemu:///system")

  set_worker_name :vmstart_worker
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
        FileUtils.mv local_filename, "#{params[:vmachines_root]}/#{params[:name]}/#{params[:hda]}"
      end

      dom.create
    rescue Exception => e
      # TODO report error by setting status
    
      # log backtarce to file
      File.open("#{RAILS_ROOT}/log/vmstart.err", "w") {|f| f.write(e.backtrace.pretty_inspect)}
    end
  end


private

  # TODO check if resource already in cache?
  def already_cached? resource_uri, cache_root
    false
  end

  # TODO make sure resource is cached
  # returns the full path of cached file, and the file will be locked
  def request_resource resource_uri, cache_root
    
    FileUtils.mkdir_p cache_root  # assure existance of cache root dir

    # TODO first, check if has local resource

    resource_filename = resource_uri[(resource_uri.rindex '/') + 1..-1]

    #logger.log resource_filename

    if resource_uri.start_with? "file://"
      # local file copy
      
      local_from = resource_uri[7..-1]
      
      local_to = "#{cache_root}/#{resource_filename}.#{rand.to_s[2..-1]}"

      FileUtils.cp local_from, local_to 

      return local_to

    elsif resource_uri.start_with? "ftp://"
      # TODO
    elsif resource_uri.start_with? "scp://"
      # TODO
    elsif resource_uri.start_with? "http://"
      # TODO
    end
  end

  # TODO try to lock resources
  # local_resource_filename does not have full path
  def lock_resource local_resoure_filename
    return false # return false: lock failed, should download new file
  end
  
  # TODO check if the resource is read only (eg. cdrom-iso)
  def resource_readonly?
  end

end

