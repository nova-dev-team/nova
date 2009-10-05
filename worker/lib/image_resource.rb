require "net/ftp"
require "uri"
require "utils"

module ImageResource

  # get a list of resource at given URI
  # only support "file://" & "ftp://" scheme
  # eg:  "file:///home/santa/"
  #      "ftp://user:password@host/path"
  def ImageResource.list_resource uri
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split uri  # parse URI information
    if scheme == "file"
      resource_list = Dir.entries(path).collect
    elsif scheme == "ftp"
      username, password = Util::split_userinfo userinfo
      Net::FTP.open(host, username, password) do |ftp|
        ftp.chdir path
        resource_list = ftp.list("*")
      end
    else
      raise "Unknown resource scheme '#{scheme}'!"
    end
    return resource_list
  end

  # get resource from given uri, save it to given file path
  # a .copying lock file will be created, and will be deleted when copying finished, whether successful or not
  def ImageResource.get_resource from_uri, to_file
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split from_uri  # parse URI information

    FileUtils.mkdir_p(File.dirname to_file)  # assure existance of file directory
    copying_lockfile = "#{to_file}.copying"

    loop do
      if (File.exist? to_file) and (File.exist? copying_lockfile)
        # both .copying & resource file exists, the file is being downloaded, only need to wait
        sleep 0.5 + rand
        # sleep with "rand" to avoid reduce race condition

      elsif (File.exist? to_file) and (not File.exist? copying_lockfile)
        # the file is already there
        break

      elsif (not File.exist? to_file) and (File.exist? copying_lockfile)
        # another process has just created an .copying lock file, and so that process will
        # download the resource, we only need to wait
        sleep 0.5 + rand

      else
        # both lock file and resource file does not exist, so it's our job to download the resource

        copying_lock = File.new(copying_lockfile, "w")
        begin
          copying_lock.flock File::LOCK_EX
    
          if scheme == "file"
            FileUtils.cp path, to_file
          elsif scheme == "ftp"
            username, password = Util::split_userinfo userinfo
            Net::FTP.open(host, username, password) do |ftp|
              ftp.chdir(File.dirname path)
              ftp.getbinaryfile((File.basename path), to_file)
            end
          else
            raise "Unknown resource scheme '#{scheme}'!"
          end

        ensure
          copying_lock.flock File::LOCK_UN
          copying_lock.close
          FileUtils.rm copying_lockfile
          break # out of the loop
        end
      end
    end

    return to_file
  end

  # save resource from local file to given remote URI
  def ImageResource.put_resource from_file, to_uri
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split to_uri  # parse URI information
  
    uploading_lockfile = "#{from_file}.uploading"
    uploading_lock = File.new(uploading_lockfile, "w")

    begin
      uploading_lock.flock File::LOCK_EX

      if scheme == "file"
        FileUtils.cp from_file, path
      elsif scheme == "ftp"
        username, password = Util::split_userinfo userinfo
        Net::FTP.open(host, username, password) do |ftp|
          ftp.chdir(File.dirname path)
          ftp.putbinaryfile(from_file, (File.basename path))
        end
      else
        raise "Unknown resource scheme '#{scheme}'!"
      end

    ensure
      uploading_lock.flock File::LOCK_UN
      uploading_lock.close
      FileUtils.rm uploading_lockfile
    end

    return to_uri
  end

  # TODO prepare resource for vmachine
  # make sure the vmachine can access the resource
  def ImageResource.prepare_vdisk vm_name, vdisk_name, device = nil, uuid = nil
    return if vdisk_name == nil or vdisk_name == "" # skip invalid arguments

    Vmachine.log vm_name, "Preparing resource: #{vdisk_name}"
    local_filename = get_resource "#{Setting.storage_server_vdisks}/#{vdisk_name}", "#{Setting.storage_cache}/#{vdisk_name}"
    vm_dir = "#{Setting.vmachines_root}/#{vm_name}"

    if Setting.image_pooling? and VdiskNaming.system_disk? vdisk_name
      local_filename = prepare_vdisk_by_pooling vm_name, vdisk_name
    end

    case VdiskNaming.vdisk_type vdisk_name
    when "sys", "sys.cow"

      if Setting.image_pooling?
        FileUtils.mv local_filename, "#{vm_dir}/#{vdisk_name}"
      else
        FileUtils.ln_s local_filename, "#{vm_dir}/#{vdisk_name}"
      end

      if device != nil and VdiskNaming.system_disk? vdisk_name
        cow_disk_name = "vd-notsaved-#{uuid}-#{device}.qcow2"
        qcow2_cmd = "qemu-img create -b #{vm_dir}/#{vdisk_name} -f qcow2 #{vm_dir}/#{cow_disk_name}"
        `#{qcow2_cmd}`
      end

    when "usr", "usr.cow"
      FileUtils.mv local_filename, "#{vm_dir}/#{vdisk_name}"
      
      # create an upload sign, so the user's data is always uploaded
      `touch #{vm_dir}/#{vdisk_name}.upload`
    when "iso"
      FileUtils.ln_s local_filename, "#{vm_dir}/#{vdisk_name}"
    else
      raise "'#{vdisk_name}' is not a valid vdisk name!"
    end

    Vmachine.log vm_name, "Ready to use: #{vdisk_name}"

  end

  def ImageResource.prepare_vdisk_by_pooling vm_name, vdisk_name
    Dir.entries(Setting.storage_cache).each do |entry|
      next unless entry.start_with? vdisk_name
      return "#{Setting.storage_cache}/#{entry}" if entry =~ /pool\.[0-9]+$/
    end

    ImageResource.copy_pool_image "#{Setting.storage_cache}/#{vdisk_name}"
  end

  
  # copy a pooling image, and return the pooled image name
  # if pooling_id is not given, automatically determines an id
  # if image already copied, return the existing image name
  def ImageResource.copy_pool_image local_filename, pooling_id = nil
    pooling_dir = File.dirname local_filename
    
    if pooling_id == nil
      local_basename = File.basename local_filename
      pooling_list = Dir.entries(pooling_dir).select {|e| e.start_with? "#{local_basename}.pool."}
      pooling_id = 1 # detect pooling id from 1
      loop do
        pooling_name = "#{local_filename}.pool.#{pooling_id}"
        break unless File.exist? pooling_name
        pooling_id += 1 # try next pooling id
      end
    end

    pooling_name = "#{local_filename}.pool.#{pooling_id}"
    unless File.exist? pooling_name
      copying_lock_fname = "#{pooling_name}.copying"
      copying_lock_file = File.new(copying_lock_fname, "w")
      File.cp local_filename, pooling_name 
      copying_lock_file.close
      FileUtils.rm copying_lock_fname
    end
    return pooling_name

  end
end
