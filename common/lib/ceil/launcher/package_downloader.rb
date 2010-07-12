require File.dirname(__FILE__) + '/../common/nfs'
require File.dirname(__FILE__) + '/../common/ftp'
require File.dirname(__FILE__) + '/../common/dir'
require 'ftools'
require 'fileutils'

class PackageDownloader
  def initialize(server_addr, port = '21', usr = 'anonymous', pwd = 'CeilClient')
    @server_addr = server_addr
    @nfs = NFSTransfer.new(server_addr)
    @ftp = FTPTransfer.new(server_addr, port, usr, pwd)
  end

  def local_exists_key(app_name)
    local_path = File.dirname(__FILE__) + "/../../keys/#{app_name}"
    if File.exists?(local_path)
      return local_path
    else
      return nil
    end
  end

  def local_exists(app_name, role = "")
    #cdrom:/packages/app_name
    #cdrom:/ceil/launcher/package_downloader.rb
    suffix = ""
    suffix = "/#{role}" if role.length > 0

    local_path = File.dirname(__FILE__) + "/../../packages/#{app_name}" + suffix
    if File.exists?(local_path)
      return local_path
    else
      return nil
    end
  end

  def download_nfs(source, dest)
    DirTool.make_clean_dir(dest)
    @nfs.download_dir(source, dest);
  end

  def download_ftp(source, dest)
    DirTool.make_clean_dir(dest)
    @ftp.download_dir(source, dest);
  end

  def package_by_nfs(app_name, char = "")
    dest = DirTool.temp_generate("install_#{app_name}")
    package_source = "/scripts/#{app_name}"
    download_nfs(package_source, dest)
    return dest
  end

  def key_by_nfs(cluster_name, app_name)
    dest = DirTool.temp_generate("key_#{app_name}")
    key_source = "/share/#{cluster_name}/#{app_name}"
    download_nfs(key_source, dest)
    return dest
  end

  def win_package_by_ftp(app_name)
    package_source = "/packages/#{app_name}"
    temp = "C:\\Temp"
    dest = "C:\\Temp\\#{app_name}"

    system "mkdir C:\\Temp"

    local_path = local_exists(app_name)
    if local_path
      FileUtils.cp_r(local_path, temp)
    else
      system "mkdir C:\\Temp\\#{app_name}"
      @ftp.download_dir(package_source, dest, "\\")
    end
    return dest
  end

  def win_shortcut_by_ftp(link_name, linkpath = 'D:\\GO\\Start\\All Programs')
    package_source = "/packages/#{link_name}"
    @ftp.download_dir(package_source, linkpath)
  end

  def package_by_ftp(app_name, char = "")
    suffix = ""
    suffix = "/#{char}" if char.length > 0
    dest = DirTool.temp_generate("install_#{app_name}_#{char}")

    local_path = local_exists(app_name, char)
    if local_path
      FileUtils.cp_r(local_path + '/.', dest)
      return dest
    else
      package_source = "/packages/#{app_name}#{suffix}"
      download_ftp(package_source, dest)
      return dest
    end
  end

  def key_by_ftp(cluster_name, app_name)
    dest = DirTool.temp_generate("key_#{app_name}")

    local_path = local_exists_key(app_name)
    if local_path
      FileUtils.cp_r(local_path + '/.', dest)
      return dest
    else
      key_source = "/keys/#{cluster_name}/#{app_name}"
      download_ftp(key_source, dest)
      return dest
    end
  end

  def download_by_carrier()
    return nil
  end
end


=begin
pd = PackageDownloader.new "166.111.131.32"
puts pd.local_exists("gundam")
puts pd.local_exists("gundam", "master")
puts pd.local_exists("gundam", "worker")
puts pd.local_exists_key("gundam")
puts pd.package_by_ftp("gundam", "master")
puts pd.package_by_ftp("gundam", "worker")
puts pd.key_by_ftp("test", "gundam")

=end
#gun = pd.win_shortcut_by_ftp("win_office");

#puts gun
#system "ls #{gun}"



