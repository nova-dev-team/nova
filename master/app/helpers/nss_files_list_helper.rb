require 'yaml'
require 'fileutils'
require "nss_proxy.rb"

module NssFilesListHelper
  private
  
    @@yaml_conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    
    # Because of NFS, nss use 127.0.0.1 for default.
    NSS_ADDR = "127.0.0.1:#{@@yaml_conf["nss_port"]}"
    
    fspath = File.join @@yaml_conf["run_root"], "nss_is_run_updater_script"
    NSS_ADDR = "#{File.open(fspath).readline}:#{common_conf["nss_port"]}" if File.exist? fspath
         
    # Check whether the nss is down.
    # Consider the nss to be down if it had been out of touch for 5 minutes, or connection to server failed.
    def nss_down?
      fpath = File.join @@yaml_conf["run_root"], "nss_is_run"
      if File.exists? fpath
        fcontent = File.read fpath
        if Time.now - File.mtime(fpath) > 5 * 60
          return true
        elsif fcontent =~ /failure/
          return true
        else
          return false
        end
      else
        return true
      end
    end
   
    # Try to update the nss files list.
    # Just touch the file, so that the back ground process will handle the request.
    def nss_try_update
      fpath = File.join @@yaml_conf["run_root"], "nss_is_run_updater_script"
      if File.exists? fpath
        FileUtils.touch fpath
        return true
      else
        return false
      end
    end
    
    # Get list of nss vdisks.
    def nss_vdisks_list
      vdisk_list = nil
      if nss_down? == false
        np = NssProxy.new NSS_ADDR
        vdisk_list = np.listdir
      end
      return vdisk_list
    end

    # Get list of agent packages.
    def nss_soft_list
      soft_list = nil
      if nss_down? == false
        np = NssProxy.new NSS_ADDR
        soft_list = np.listdir "agent_packages"
      end
      return soft_list
    end
end
