# This controller is used to do miscellanous works. Generally speaking, it supports other controllers.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'fileutils'

class MiscController < ApplicationController

  # Reply the role of this node. When adding workers, master uses this service to "authenticate" workers.
  #
  # Since::     0.3
  def role
    reply_success "worker"
  end

  def revoke_vm_image
    unless valid_param? params[:image_name]
      reply_failure "Please provide 'image_name'!"
    else
      has_revoked_some_files = false
      Dir.foreach(Setting.image_pool_root) do |entry|
        next if entry.start_with? "." or entry.end_with? ".copying" or entry.end_with? ".revoke"
        next unless entry.start_with? params[:image_name]
        fpath = File.join Setting.image_pool_root, entry
        copying_lock = fpath + ".copying"
        if entry.end_with? ".qcow2" and File.exists? copying_lock
          # downloading the qcow2 image, ignore revoke request
          next
        end
        if File.exists? copying_lock
          # The file is being copied, create a revoke flag for it. The deleting job is left for image_pool_maintainer and trash_cleaner
          revoke_fn = fpath + ".revoke"
          FileUtils.touch revoke_fn
        else
          FileUtils.rm_f fpath
        end
        has_revoked_some_files = true
      end
      if has_revoked_some_files
        reply_success "Image with name='#{params[:image_name]}' is revoked!"
      else
        reply_success "Image with name='#{params[:image_name]}' not found, nothing revoked!"
      end
    end
  end

end
