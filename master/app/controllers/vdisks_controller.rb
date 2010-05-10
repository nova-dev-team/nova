# The controller for VM disk images.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3


class VdisksController < ApplicationController

  include FtpServerFilesListHelper

  before_filter :login_required

  # List all the registered vdisks.
  #
  # Since::     0.3
  def list
    reply_model Vdisk, :items => ["file_name", "display_name", "description", "disk_format", "os_family", "os_name", "soft_list"]
  end

  # Register a new vdisk.
  #
  # Since::     0.3
  def register
    return unless root_required
    return unless params_required "file_name display_name disk_format"

    # Check if file exists, by checking the 'ftp_server_files_list'
    vdisk_list = ftp_server_vdisks_list
    if vdisk_list == nil
      reply_failure "The storage server is probably down!"
      return
    end
    unless vdisk_list.include? params[:file_name]
      ftp_server_try_update
      reply_failure "Cannot find '#{params[:file_name]}' on storage server. You might need to wait a few minutes if the file has just been uploaded."
      return
    end

    if (Vdisk.find_by_display_name params[:display_name])
      reply_failure "The display_name '#{params[:display_name]}' has already been used!"
      return
    end

    vd = Vdisk.new
    vd.file_name = params[:file_name]
    vd.display_name = params[:display_name]
    vd.description = params[:description]
    vd.disk_format = params[:disk_format]
    vd.os_family = params[:os_family]
    vd.os_name = params[:os_name]
    vd.save
    reply_success "Successfully added vdisk '#{params[:file_name]}'!"
  end

  # Remove a registered vdisk.
  #
  # Since::     0.3
  def remove
    return unless root_required
    unless valid_param? params[:file_name]
      reply_failure "Please provide the 'file_name' parameter!"
      return
    end
    vd = Vdisk.find_by_file_name params[:file_name]
    if vd == nil
      reply_failure "Cannot find Vdisk with 'file_name=#{params[:file_name]}'!"
      return
    end
    Vdisk.delete vd
    reply_success "Vdisk with 'file_name=#{params[:file_name]}' is deleted!"
  end

  # Set the software list of a vdisk.
  # 'soft_list' is separated by comma.
  #
  # Since::     0.3
  def edit_soft_list
    return unless root_required

    # note that 'soft_list' could be '', so we don't use 'valid_param?' here.
    unless valid_param? params[:file_name] and params[:soft_list] != nil
      reply_failure "Please provide valid 'file_name' and 'soft_list' parameter!"
      return
    end
    vd = Vdisk.find_by_file_name params[:file_name]
    if vd == nil
      reply_failure "Cannot find Vdisk with 'file_name=#{params[:file_name]}'!"
      return
    end

    soft_list = params[:soft_list].split /,| /
    soft_not_found = []
    soft_list.each do |soft|
      unless Software.find_by_file_name soft
        soft_not_found << soft
      end
    end
    if soft_not_found.length > 0
      reply_failure "Cannot find software with file name '#{soft_not_found.join "', '"}'"
      return
    end
    vd.soft_list = soft_list.join ","
    vd.save
    reply_success "Updated list of software for '#{params[:file_name]}'", :vdisk => params[:file_name], :soft_list => soft_list
  end
end

