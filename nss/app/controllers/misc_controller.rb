require "fileutils"

class MiscController < ApplicationController
  def role
    reply_success "storage"
  end

  def version
    if File.exists? "#{RAILS_ROOT}/../VERSION"
      ver = File.read("#{RAILS_ROOT}/../VERSION").strip
      reply_success "Version is '#{ver}'", :version => ver
    else
      reply_failure "Version unknown!"
    end
  end

  def hostname
    if File.exists? "#{RAILS_ROOT}/tmp/hostname"
      hostname = File.read("#{RAILS_ROOT}/tmp/hostname").strip
      reply_success "Hostname is '#{hostname}'", :hostname => hostname
    else
      reply_failure "Fail to get hostname!"
    end
  end

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

    def revoke_package
      unless valid_param? params[:]
  end
