require 'net/ftp'
require 'fileutils'
require 'pp'

module VdisksHelper

  class Helper

    def Helper.file_exist? file_uri
      scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split file_uri  # parse URI information

      if scheme == "file"
        File.exist? path
      elsif scheme == "ftp"
        username, password = Util::split_userinfo userinfo
        list = []
        Net::FTP.open(host, username, password) do |ftp|
          ftp.chdir(File.dirname path)
          list = ftp.list(File.basename path)
        end
        return list.size != 0
      else
        raise "Resource scheme '#{scheme}' not known!"
      end
    end

    def Helper.move_file server_root, from_path, to_path
      scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split server_root  # parse URI information

      if scheme == "file"
        FileUtils.mv "#{path}/#{from_path}", "#{path}/#{to_path}"
      elsif scheme == "ftp"
        username, password = Util::split_userinfo userinfo
        Net::FTP.open(host, username, password) do |ftp|
          ftp.rename from_path, to_path
        end
      else
        raise "Resource scheme '#{scheme}' not known!"
      end
    end

    # show a list of files on storage server
    def Helper.upload_list
      list_files ((Setting.find_by_key "storage_server").value + "/vdisks_upload")
    end

    # a copy from worker's vmachines_helper
    def Helper.list_files dir_uri
      scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split dir_uri  # parse URI information

      files_list = []
      if scheme == "file"
        Dir.new(path).entries.each do |entry|
          files_list << entry
        end
      elsif scheme == "ftp"
        username, password = Util::split_userinfo userinfo
        Net::FTP.open(host, username, password) do |ftp|
          ftp.chdir path
          files_list = ftp.list("*").map {|vd| vd[(vd.rindex(" ") + 1)..-1]}
        end
      else
        raise "Resource scheme '#{scheme}' not known!"
      end
      return files_list
    end
  end

end

# a copy from worker's util
module Util

  def Util.split_userinfo userinfo
    index = userinfo.index ":"
    username = userinfo[0...index]
    password = userinfo[(index + 1)..-1]
    return username, password
  end

  def split_userinfo userinfo
    Util.split_userinfo
  end

end
