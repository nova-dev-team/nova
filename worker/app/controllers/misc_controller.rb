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

  # Show the features of Nova platform.
  #
  # Since::   0.3
  def feat
    conf = common_conf
    reply_success "Query successful!", :feat => {
      :enable_migration => conf["vm_enable_migration"],
      :hypervisor => conf["hypervisor"]
    }
  end

  # return worker's uuid
  # Since::   0.3.1
  def uuid
    if File.exists? "#{RAILS_ROOT}/config/worker.uuid"
      uuid = File.read("#{RAILS_ROOT}/config/worker.uuid").strip
      reply_success "Worker UUID is '#{ver}'", :uuid => uuid
    else
      reply_failure "UUID is not specified!"
    end
  end
  
  # Reply the current version of Nova platform.
  #
  # Since::   0.3
  def version
    if File.exists? "#{RAILS_ROOT}/../VERSION"
      ver = File.read("#{RAILS_ROOT}/../VERSION").strip
      reply_success "Version is '#{ver}'", :version => ver
    else
      reply_failure "Version unknown!"
    end
  end

  # Returns the running rails environtment.
  #
  # Since::   0.3
  def rails_env
    reply_success "Rails environment is '#{Rails.env}'", :env => Rails.env
  end

  # Get the hostname of the machine.
  #
  # Since::     0.3
  def hostname
    if File.exists? "#{RAILS_ROOT}/tmp/hostname"
      hostname = File.read("#{RAILS_ROOT}/tmp/hostname").strip
      reply_success "Hostname is '#{hostname}'", :hostname => hostname
    else
      reply_failure "Failed to get hostname!"
    end
  end


  # Removes deprecated VM image.
  #
  # Since::     0.3
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

  # Removes deprecated VM packages.
  #
  # Since::     0.3
  def revoke_package
    unless valid_param? params[:package_name]
      reply_failure "Please provide 'package_name'!"
    else
      pkg_dir = File.join Setting.package_pool_root, params[:package_name]
      pkg_copying_lock = "#{pkg_dir}.copying"
      pkg_lftp_log = "#{pkg_dir}.lftp.log"
      if File.exists? pkg_copying_lock
        reply_failure "Cannot revoke package '#{params[:package_name]}', it is being used now."
      else
        has_revoked_some_files = false
        if File.exists? pkg_lftp_log
          FileUtils.rm_rf pkg_lftp_log
          has_revoked_some_files = true
        end
        if File.exists? pkg_dir
          FileUtils.rm_rf pkg_dir
          has_revoked_some_files = true
        end
        if has_revoked_some_files
          reply_success "Package with name='#{params[:package_name]}' is revoked!"
        else
          reply_success "Package with name='#{params[:package_name]}' not found, nothing revoked!"
        end
      end
    end
  end

end
