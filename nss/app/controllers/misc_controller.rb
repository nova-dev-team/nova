# This contoller is used to do miscellanous works. Generally speaking, it supports other controllers.

require "fileutils"

class MiscController < ApplicationController
# Reply the role of this node. When adding storages, master uses this service to "authenticate" workers.
  def role
    reply_success "storage"
  end

# Reply the current version of Nova platform.
  def version
    if File.exists? "#{RAILS_ROOT}/../VERSION"
      ver = File.read("#{RAILS_ROOT}/../VERSION").strip
      reply_success "Version is '#{ver}'", :version => ver
    else
      reply_failure "Version unknown!"
    end
  end

# Get the hostname of the machine.
  def hostname
    if File.exists? "#{RAILS_ROOT}/tmp/hostname"
      hostname = File.read("#{RAILS_ROOT}/tmp/hostname").strip
      reply_success "Hostname is '#{hostname}'", :hostname => hostname
    else
      reply_failure "Fail to get hostname!"
    end
  end

# Removes deprecated VM image.
  def revoke_vm_image
    dir = "#{RAILS_ROOT}/../../run/image_pool"
    fpath = ""
    unless valid_param? params[:image_name]
      reply_failure "Please provide 'image_name'"
    else
      has_revoked_some_files = false
      Dir.foreach(dir.to_s) do |entry|
       next if entry.start_with? "." or entry.end_with? ".copying" or entry.end_with? ".revoke"
       next unless entry.start_with? params[:image_name]
        fpath = dir + "/" + entry
        copying_lock = fpath + ".copying"
        if entry.end_with? ".qcow2" and File.exists? copying_lock
           next
        end
        if File.exists? copying_lock
           revoke_fn = fpath + ".revoke"
           FileUtils.touch revoke_fn
         else
           FileUtils.rm_f fpath
        end
       has_revoked_some_files = true
       end
       if has_revoked_some_files == true
        reply_success "Image with name = '#{params[:image_name]}' is revoked!" 
       else
        reply_success "Image with name = '#{params[:image_name]}' not found, nothing revoked!"
       end
      end
    end

# Removes deprecated VM packages.
    def revoke_package
      dir = "#{RAILS_ROOT}/../../run/package_pool"
      pkg_path = ""
      unless valid_param? params[:package_name]
        reply_failure "Please provide 'package_name'!"
      else
        pkg_path = dir + "/" + params[:package_name]
        pkg_copying_lock = "#{pkg_path}.copying"
        pkg_lftp_log = "#{pkg_path}.lftp.log"
        if File.exists? pkg_copying_lock
          reply_failure "Cannot revoke package '#{params[:package_name]}', it is being used now."
        else
          has_revoked_some_files = false
          if File.exists? pkg_lftp_log
            FileUtils.rm_rf pkg_lftp_log
            has_revoked_some_files = true
          end
          if File.exists? pkg_path
            FileUtils.rm_rf pkg_path
            has_revoked_some_files = true
          end
          if has_revoked_some_files
            reply_success "Package with name = '#{params[:package_name]}' is revoked!"
          else
            reply_success "Package with name = '#{params[:package_name]}' not found, nothing revoked!"
          end
        end
      end
    end

  end
