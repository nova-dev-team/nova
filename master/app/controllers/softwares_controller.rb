# The controller for VM packages.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3


class SoftwaresController < ApplicationController

  include FtpServerFilesListHelper

  before_filter :login_required

  # List all the registered softwares.
  #
  # Since::     0.3
  def list
    reply_model Software, :items => ["file_name", "display_name", "description", "os_family"]
  end

  # Register a new software.
  #
  # Since::     0.3
  def register
    return unless root_required
    return unless params_required "file_name display_name"

    # Check if file exists, by checking the 'ftp_server_files_list'
    soft_list = ftp_server_soft_list
    if soft_list == nil
      reply_failure "The storage server is probably down!"
      return
    end
    unless soft_list.include? params[:file_name]
      ftp_server_try_update
      reply_failure "Cannot find '#{params[:file_name]}' on storage server. You might need to wait a few minutes if the file has just been uploaded."
      return
    end

    if (Software.find_by_display_name params[:display_name])
      reply_failure "The display_name '#{params[:display_name]}' has already been used!"
      return
    end

    soft = Software.new
    soft.file_name = params[:file_name]
    soft.display_name = params[:display_name]
    soft.description = params[:description]
    soft.os_family = params[:os_family]
    soft.save
    reply_success "Successfully added software '#{params[:file_name]}'!"
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
    soft = Software.find_by_file_name params[:file_name]
    if soft == nil
      reply_failure "Cannot find software with 'file_name=#{params[:file_name]}'!"
      return
    end
    affected_vdisks = []
    Vdisk.all.each do |vd|
      next if vd.soft_list == nil
      vd_soft_list = vd.soft_list.split ","
      updated = false
      if vd_soft_list.include? soft.file_name
        vd_soft_list.delete soft.file_name
        updated = true
      end
      if updated
        vd.soft_list = vd_soft_list.join ","
        vd.save
        affected_vdisks << vd.file_name
      end
    end
    Software.delete soft
    reply_success "Software with 'file_name=#{params[:file_name]}' is deleted!", :affected_vdisks => affected_vdisks
  end

  # Automatically add to vdisks, by matching "os_family",
  #
  # Since::   0.3
  def apply_to_vdisks
    return unless root_required
    unless valid_param? params[:file_name]
      reply_failure "Please provide the 'file_name' parameter!"
      return
    end
    soft = Software.find_by_file_name params[:file_name]
    if soft == nil
      reply_failure "Cannot find software with 'file_name=#{params[:file_name]}'!"
      return
    end
    applied_vdisk = []
    Vdisk.all.each do |vd|
      if vd.os_family == soft.os_family
        if vd.soft_list == nil
          vd_soft_list = []
        else
          vd_soft_list = vd.soft_list.split ","
        end
        updated = false
        unless vd_soft_list.include? soft.file_name
          vd_soft_list << soft.file_name
          updated = true
        end
        if updated
          vd.soft_list = vd_soft_list.join ","
          vd.save
          applied_vdisk << vd.file_name
        end
      end
    end
    if applied_vdisk.length != 0
      reply_success "Applied software '#{params[:file_name]}' to vdisks: '#{applied_vdisk.join "', '"}'", :soft => params[:file_name], :applied_vdisk => applied_vdisk
    else
      reply_success "Nothing changed", :soft => params[:file_name], :applied_vdisk => applied_vdisk
    end
  end

end

