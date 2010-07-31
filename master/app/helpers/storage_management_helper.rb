require 'yaml'
require 'fileutils'
require 'utils'
require "nss_proxy.rb"
require File.dirname(__FILE__) + "/ftp_server_files_list_helper.rb"
require File.dirname(__FILE__) + "/nss_files_list_helper.rb"

module StorageManagementHelper

  @@conf = common_conf
  include FtpServerFilesListHelper
  include NssFilesListHelper

  # Try to update the storage servers file list.
  # Just touch the file, so that the back ground process will handle the request.
  #
  # Since::     0.3
  def storage_server_try_update
    if @@conf["storage_tpye"] == "ftp"
      return ftp_server_try_update
    elsif @@conf["storage_type"] == "nfs"
      return nss_try_update
    else
      return nil
    end
  end
  # Check if the storage (ftp or nfs)server is down.
  # Consider the storage server to be down if it had been out of touch for 5 minutes, or connection to server failed.
  #
  # Since::     0.3
  def storage_server_down?
    if @@conf["storage_tpye"] == "ftp"
      return ftp_server_down?
    elsif @@conf["storage_type"] == "nfs"
      return nss_down?
    else
      return nil
    end
  end
  # Get list of storage server vdisks.
  # Returns nil on failure.
  #
  # Since::   0.3
  def storage_server_vdisks_list
    if @@conf["storage_tpye"] == "ftp"
      return ftp_server_vdisks_list
    elsif @@conf["storage_type"] == "nfs"
      return nss_vdisks_list
    else
      return nil
    end
  end
  # Get list of storage packages.
  # Returns nil on failure.
  #
  # Since::   0.3
  def storage_server_soft_list
    if @@conf["storage_tpye"] == "ftp"
      return ftp_server_soft_list
    elsif @@conf["storage_type"] == "nfs"
      return nss_soft_list
    else
      return nil
    end
  end
end
