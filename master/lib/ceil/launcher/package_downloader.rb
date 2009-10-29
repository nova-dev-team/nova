require File.dirname(__FILE__) + '/../common/nfs'
require File.dirname(__FILE__) + '/../common/ftp'
require File.dirname(__FILE__) + '/../common/dir'

class PackageDownloader
  def initialize(server_addr)
    @server_addr = server_addr
    @nfs = NFSTransfer.new(server_addr)
    @ftp = FTPTransfer.new(server_addr)
  end

  def download_nfs(source, dest)
    DirTool.make_clean_dir(dest)
    @nfs.download_dir(source, dest);
  end
  def download_ftp(source, dest)
    DirTool.make_clean_dir(dest)
    @ftp.download_dir(source, dest);
  end

  def package_by_nfs(app_name)
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

  def package_by_ftp(app_name)
    dest = DirTool.temp_generate("install_#{app_name}")
    package_source = "/packages/#{app_name}"
    download_ftp(package_source, dest)
    return dest
  end

  def key_by_ftp(cluster_name, app_name)
    dest = DirTool.temp_generate("key_#{app_name}")
    key_source = "/keys/#{cluster_name}/#{app_name}"
    download_ftp(key_source, dest)
    return dest
  end

  def download_by_carrier()
    return nil
  end
end

#pd = PackageDownloader.new "192.168.0.110"
#gun = pd.download_by_nfs("mpi");
#puts gun
#system "ls #{gun}"



