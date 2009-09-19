require 'rubygems'
require 'libvirt'
require 'fileutils'
require 'ftools'
require 'pp'
require 'uri'
require 'net/ftp'

class VmachinesWorker < BackgrounDRb::MetaWorker

  include VdiskNaming
  include Util
  
  @@virt_conn = VmachinesHelper::Helper.virt_conn

  MAX_COPYING_TRY_COUNT = 5 # after 5 failed tries, we stop copying resource
  MAX_STALL_TIME = 30 # after stalling for 30 seconds, we think the resource has failed to download

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

      params[:depend].split.each do |dep|
        get_resource dep, params[:name], progress
      end

      get_resource params[:cdrom], params[:name], progress
      get_resource params[:hda], params[:name], progress, "hda", params[:uuid]
      get_resource params[:hdb], params[:name], progress, "hdb", params[:uuid]

      progress[:info] = "starting vmachine '#{params[:name]}'"
      progress[:status] = "in progress"
      progress.save
      
      begin
        dom.create
        Progress.delete progress
      rescue Libvirt::Error => e
        progress[:info] = "create vmachine failed, possibly caused by lack of resource"
        progress[:status] = "create failure"
        progress.save
      end

    rescue Exception => e
      progress[:info] = "error on server side, check log files"
      progress[:status] = "error occured"
      progress.save

      # log backtarce to file
      logger.debug e.pretty_inspect.to_s + "\n" + e.backtrace.pretty_inspect.to_s
    end
  end

  # cleanup after a vmachine is destroyed
  # The vmachines must have been successfully ran (must be a healthy one)
  def do_cleanup args
    begin
      vm_dir = "#{args[:vmachines_root]}/#{args[:vmachine_name]}"
      logger.debug "doing cleanup in #{vm_dir}!"

      Dir.foreach(vm_dir) do |entry|
        if entry.end_with? ".upload"
          file_to_upload = entry[0...-7]
          progress = Progress.new
          progress[:owner] = args[:vmachine_name]
          progress[:info] = "uploading image '#{file_to_upload}'"
          progress[:status] = "in progress"
          progress.save
          put_file "#{vm_dir}/#{file_to_upload}", "#{Setting.storage_server_vdisks}/#{file_to_upload}"
          Progress.delete progress
        end
      end

      # remove dirty progress information, if exists (ususlly when the machine failed to start)
      Progress.delete_all ["owner = ?", args[:vmachine_name]]

      # remove local resource
      logger.debug "*** [remove] #{vm_dir}"
      FileUtils.rm_rf vm_dir
    rescue Exception => e
      logger.error e.pretty_inspect.to_s + "\n" + e.backtrace.pretty_inspect.to_s
    end
  end

  # called every a few seconds, check if local files are in good health
  def supervise
    logger.debug "Supervisor running at #{Time.now}"
    
    storage_cache_root = Setting.storage_cache
    FileUtils.mkdir_p storage_cache_root

    # supervise on stoarge cache (using .copying files)
    Dir.foreach(storage_cache_root) do |entry|
      next unless entry =~ /\.copying/

      logger.debug "supervising '#{entry}' as copying lock file"
      resource_filename = "#{storage_cache_root}/#{entry[0...-8]}" # notice this is the full path name
      logger.debug "checking status of resource file '#{resource_filename}'"
      if File.exist? resource_filename # have resource file, create/update supervise file
        supervise_filename = "#{resource_filename}.supervise"
        if File.exist? supervise_filename # exists supervise file, update it
          supervise_file = File.new(supervise_filename, "r")
          old_last_check_size = -1
          try_count = -1
          last_change_time = -1
          supervise_file.readlines.each do |line|
            items = line.chomp.split '='
            logger.debug "Split line=#{items.pretty_inspect}"
            case items[0]
            when "try_count"
              try_count = items[1].to_i
              logger.debug "in #{File.basename supervise_filename}: try_count = #{try_count}"
            when "last_check_size"
              old_last_check_size = items[1].to_i
              logger.debug "in #{File.basename supervise_filename}: last_check_size = #{old_last_check_size}"
            when "last_change_time_i"
              last_change_time = items[1].to_i
              logger.debug "in #{File.basename supervise_filename}: last_change_time_i = #{last_change_time}"
            end
          end
          supervise_file.close

          last_check_size = File.size resource_filename
          if last_check_size != old_last_check_size # the file size is changed
            last_change_time = Time.now.to_i
          else # file size not changed, we should delete it if stalled for a long time
            stall_time = Time.now.to_i - last_change_time
            if stall_time > MAX_STALL_TIME # waited for too long, we think the resource failed to download
              # simply delete both .coying file and resource file, they will be started downloading again
              try_count += 1
              FileUtils.rm resource_filename
              FileUtils.rm "#{resource_filename}.copying"
              last_change_time = Time.now.to_i
            end
          end

          supervise_file = File.new(supervise_filename, "w")
          supervise_file.write "try_count=#{try_count}\n"
          supervise_file.write "last_check_time=#{Time.now}\n"
          supervise_file.write "last_check_size=#{last_check_size}\n"
          supervise_file.write "last_change_time_i=#{last_change_time}\n"
          supervise_file.close
        else
          supervise_file = File.new(supervise_filename, "w")
          supervise_file.write "try_count=1\n"
          supervise_file.write "last_check_time=#{Time.now}\n"
          supervise_file.write "last_check_size=#{File.size resource_filename}\n"
          supervise_file.write "last_change_time_i=#{Time.now.to_i}\n"
          supervise_file.close
        end

      else # no such resource file, report error
        logger.debug "resource file '#{resource_filename}' is not found! deleting copying lock file"
        FileUtils.rm "#{resource_filename}.copying"
      end
    end

  end

  # called every a few minutes, check if there is new resource on storage server
  def update
    # TODO updater
    logger.debug "Updater runing at #{Time.now}"

    # update file listing
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split Setting.storage_server_vdisks  # parse URI information
    list = VmachinesHelper::Helper.list_files Setting.storage_server_vdisks
    File.open(Setting.resource_list_cache, "w") do |file|
      file.write Time.now.to_s + "\n"
      if scheme == "file"
        list.each do |entry|
          next if entry.start_with? "."
          file.write File.size("#{path}/#{entry}").to_pretty_file_size + "\t#{entry}\n"
        end
      elsif scheme == "ftp"
        list.each do |entry|
          file.write entry + "\n"
        end
      else
        raise "Scheme '#{scheme}' not known!"
      end
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

    loop do
      if (File.exist? local_to) and (File.exist? copying_lock_filename)
        # if both the lock and resource exists
        # in this situation, the file is being downloaded, so we only need to wait
        sleep (0.5 + rand)
        # sleep with 'rand' is important. it is possible that 2 or more requests arrive at same time
        # and are waiting for same resource. sleeping with 'rand' might reduce race condition
      elsif (File.exist? local_to) and (not File.exist? copying_lock_filename)
        # resource exists, but lock is not around, this means the resource is ready to be used
        break # out of the loop
      elsif (not File.exist? local_to) and (File.exist? copying_lock_filename)
        # only lock file exists, this is an wrong condition, we should count on 'supervise' method
        # do nothing but sleep a while
        sleep (0.5 + rand)
      else # both lock file and resource file does not exist, download them now (if try_count not > MAX TRY COUNT)
        
        # check if try_count is too big
        supervise_filename = "#{resource_filename}.supervise"
        if File.exist? supervise_filename
          supervise_file = File.new(supervise_filename, "r")
          supervise_file.readlines.each do |line|
            items = line.split "="
            try_count = items[1].to_i if items[0] == "try_count"
            if try_count > MAX_COPYING_TRY_COUNT
              break # out of the loop
            end
          end
          supervise_file.close
        end

        copying_lock = File.new(copying_lock_filename, "w")
        begin # it is highly possible to have exception, so we have to handle it
          # both the lock file and resource are not here, so we should start downloading now
          # it is hightly possible to have excption, so we must handle that
          copying_lock.flock File::LOCK_EX

          get_file resource_uri, local_to

        rescue
          sleep (0.5 + rand) # sleep a while, maybe the supervisor will handle errors
        ensure
          copying_lock.flock File::LOCK_UN
          copying_lock.close
          FileUtils.rm copying_lock_filename

          break # out of the loop
        end
      end
    end

    return local_to
  end


  # get a file from some uri, and save to a file
  #
  # assumption: from_uri accessable, in schemes of "ftp, scp(usually fail, don't use it), file, carrierfs?"
  #             to_file does not exist
  def get_file from_uri, to_file
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split from_uri  # parse URI information
  
    logger.debug "Retrieving file from: #{from_uri}"

    FileUtils.mkdir_p(File.dirname to_file)  # assure existance of file directory
    if scheme == "file"
      FileUtils.cp path, to_file
    elsif scheme == "ftp"
      username, password = Util::split_userinfo userinfo
      Net::FTP.open(host, username, password) do |ftp|
        ftp.chdir(File.dirname path)
        ftp.getbinaryfile((File.basename path), to_file)
      end
    else
      raise "Resource scheme '#{scheme}' not known!"
    end
  end

  def put_file from_file, to_uri
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split to_uri  # parse URI information
  
    logger.debug "Put file to #{to_uri}"

    if scheme == "file"
      FileUtils.cp from_file, path
    elsif scheme == "ftp"
      username, password = Util::split_userinfo userinfo
      Net::FTP.open(host, username, password) do |ftp|
        ftp.chdir(File.dirname path)
        ftp.putbinaryfile(from_file, (File.basename path))
      end
    else
      raise "Resource scheme '#{scheme}' not known!"
    end
  end


  def get_resource resource_name, vmachine_name, progress, device = nil, uuid = nil
    return if resource_name == nil or resource_name == ""

    vmachine_dir = "#{Setting.vmachines_root}/#{vmachine_name}"

    return if File.exist? "#{vmachine_dir}/#{resource_name}" # skip, if file already exists

    progress[:info] = "downloading: #{resource_name}"
    progress[:status] = "in progress"
    progress.save

    local_filename = request_resource "#{Setting.storage_server_vdisks}/#{resource_name}", Setting.storage_cache
    `chmod 400 #{Setting.storage_cache}/#{resource_name}` # make sure the image file is readonly

    case vdisk_type resource_name
    when "sys", "sys.cow"
      FileUtils.ln local_filename, "#{vmachine_dir}/#{resource_name}"
      if device != nil # when using this disk as a device (not extra dependency), we must make a COW disk based on it
        cow_disk_name = "vd-notsaved-#{uuid}-#{device}.qcow2"
        qcow2_cmd = "qemu-img create -b #{vmachine_dir}/#{resource_name} -f qcow2 #{vmachine_dir}/#{cow_disk_name}"
        logger.debug "*** [cmd] #{qcow2_cmd}"
        `#{qcow2_cmd}`
      end
    when "user", "user.cow"
      FileUtils.mv local_filename, "#{vmachine_dir}/#{resource_name}"
      # create upload sign file, user's data is always uploaded
      `touch #{vmachine_dir}/#{resource_name}.upload`
    when "iso"
      FileUtils.ln_s local_filename, "#{vmachine_dir}/#{resource_name}"
    else
      raise "'#{resource_name}' is not a valid resource name!"
    end

  end


end

