# Controller for file system operation.

require 'fileutils'
require 'utils'

class FsController < ApplicationController

# Show a listing of file directory.
# * params[:dir]: directory name of items relativing to /nova/storage, eg:
#    /fs/listdir?dir=vdisks

  def  listdir
    # by default, list the "vdisks" dir
    params[:dir] = "vdisks" unless valid_param? params[:dir]
    if params[:dir].start_with? "/"
      # absolute path, use it directly
      dir = Pathname.new(params[:dir]).cleanpath.to_s
    else
      dir = Pathname.new(File.join common_conf["storage_root"], params[:dir]).cleanpath.to_s
    end
    begin
      data = []
      Dir.foreach(dir.to_s) do |entry|
        next if entry.start_with? '.'
        fpath = File.join dir, entry
        data << {
          :filename => entry,
          :fsize => (File.size fpath),
          :isdir => (File::directory? fpath)
        }
      end
      data = data.sort {|x, y| x[:filename] <=> y[:filename]}
      reply_success "Query successful!", :data => data, :dir => dir
    rescue Exception => e
      reply_failure "Directory '#{dir}' not found! Raw error message: #{e.to_s}"
    end
  end

# Delete a file or a directory.
# * params[:path]: path of the file(directory) to be deleted. (could be absolute path, or relative path to 'nova storage' dir)
  def rm
    if valid_param? params[:path]
      if params[:path].start_with? "/"
        # absolute path, use it directly
        path = params[:path]
      else
        path = File.join common_conf["storage_root"], params[:path]
      end
      begin
        FileUtils.rm_r path.to_s
        reply_success "Delete successful!", :path => params[:path]
      rescue Exception => e
        reply_success "Failed to remove '#{path}'. Raw error message: #{e.to_s}"
      end
    else
      reply_failure "Please provide the 'path' parameter!"
    end
  end

# Move a file or a directory.
# * params[:from]: the src path of the file(directory);
# * params[:to]: the dest path of the file(directory).
  def mv
    if (valid_param? params[:from]) && (valid_param? params[:to])
      if params[:from].start_with? "/"
        from = params[:from]
      else
        from = File.join common_conf["storage_root"], params[:from]
      end
      if params[:to].start_with? "/"
        to = params[:to]
      else
        to = File.join common_conf["storage_root"], params[:to]
      end
      begin
        FileUtils.mv(from.to_s, to.to_s)
        reply_success "Move successful!", :from => params[:from], :to => params[:to]
      rescue Exception => e
        reply_failure "Failed to move '#{from}' to '#{to}'. Raw error message: #{e.to_s}"
      end
    else
        reply_failure "Please provide the 'from' & 'to' parameters!"
    end
  end

# Copy a file.
# * params[:from]: the src path of the file;
# * params[:to]: the dest path of the file.
  def cp
    if (valid_param? params[:from]) && (valid_param? params[:to])
      if params[:from].start_with? "/"
        from = params[:from]
      else
        from = File.join common_conf["storage_root"], params[:from]
      end
      if params[:to].start_with? "/"
        to = params[:to]
      else
        to = File.join common_conf["storage_root"], params[:to]
      end
      fork do
        File.new(to.to_s + ".copying", "w")
        system("cp #{from.to_s} #{to.to_s}")
        File.delete(to.to_s + ".copying")
      end
      reply_success "Copy successful!", :from => params[:from], :to => params[:to]
      #  rescue Exception => e
      #  reply_failure "Failed to copy '#{from}' to '#{to}'. Raw error message: #{e.to_s}"
      #  end
    else
      reply_failure "Please provide the 'from' & 'to' parameters!"
    end
  end
end
