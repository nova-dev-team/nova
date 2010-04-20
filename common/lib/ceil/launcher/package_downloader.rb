require File.dirname(__FILE__) + '/../common/nfs'
require File.dirname(__FILE__) + '/../common/ftp'
require File.dirname(__FILE__) + '/../common/dir'
require 'ftools'
require 'fileutils'

class PackageDownloader
	def initialize(server_addr)
		@server_addr = server_addr
		@nfs = NFSTransfer.new(server_addr)
		@ftp = FTPTransfer.new(server_addr)
	end

	def local_exists(app_name)
		#cdrom:/packages/app_name
		#cdrom:/ceil/launcher/package_downloader.rb

		local_path = File.dirname(__FILE__) + "/../../packages/#{app_name}"
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
		dest = "C:\\Temp\\#{app_name}"
		system "mkdir C:\\Temp"
		system "mkdir C:\\Temp\\#{app_name}"
		
		local_path = local_exists(app_name)
		if local_path
			FileUtils.cp_r(local_path, dest)
		else
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
		
		package_source = "/packages/#{app_name}#{suffix}"
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

#pd = PackageDownloader.new "166.111.131.32"
#puts pd.local_exists("gundam")

#gun = pd.win_shortcut_by_ftp("win_office");

#puts gun
#system "ls #{gun}"



