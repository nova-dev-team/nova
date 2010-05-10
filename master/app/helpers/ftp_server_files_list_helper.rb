# This module is used to help retrieve info from ftp_server_files_list.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'yaml'
require "fileutils"

module FtpServerFilesListHelper

private

  @@yaml_conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"

  # Check if the server is down.
  # Consider the server to be down if it had been out of touch for 5 minutes, or connection to server failed.
  #
  # Since::     0.3
  def ftp_server_down?
    fpath = File.join @@yaml_conf["run_root"], "ftp_server_files_list"
    if File.exists? fpath
      fcontent = File.read fpath
      if Time.now - File.mtime(fpath) > 5 * 60
        return true
      elsif fcontent =~ /Fatal error: max-retries exceeded/ or fcontent =~ /Not connected/ or fcontent =~ /Name or service not known/
        return true
      else
        return false
      end
    else
      return true
    end
  end

  # Try to update the ftp servers file list.
  # Just touch the file, so that the back ground process will handle the request.
  #
  # Since::     0.3
  def ftp_server_try_update
    fpath = File.join @@yaml_conf["run_root"], "ftp_server_files_list"
    if File.exists? fpath
      FileUtils.touch fpath
      return true
    else
      return false
    end
  end

  # Get list of ftp server vdisks.
  # Returns nil on failure.
  #
  # Since::   0.3
  def ftp_server_vdisks_list
    fpath = File.join @@yaml_conf["run_root"], "ftp_server_files_list"
    fcontent = File.read fpath

    vdisk_list = nil
    fcontent.each_line do |line|
      line = line.strip
      if line.start_with? "ftp://" and line.end_with? "vdisks"
        vdisk_list = []
        next
      end
      if vdisk_list != nil
        if line.start_with? "ftp://"
          # end of list
          break
        else
          entry = line.split[8]
          next if entry.start_with? "."
          vdisk_list << entry
        end
      end
    end
    return vdisk_list
  end

  # Get list of agent packages.
  # Returns nil on failure.
  #
  # Since::   0.3
  def ftp_server_soft_list
    fpath = File.join @@yaml_conf["run_root"], "ftp_server_files_list"
    fcontent = File.read fpath

    soft_list = nil
    fcontent.each_line do |line|
      line = line.strip
      if line.start_with? "ftp://" and line.end_with? "agent_packages"
        soft_list = []
        next
      end
      if soft_list != nil
        if line.start_with? "ftp://"
          # end of list
          break
        else
          entry = line.split[8]
          next if entry.start_with? "."
          soft_list << entry
        end
      end
    end
    return soft_list
  end

end
