# Worker module proxy, used by master's daemons.
# It delegates all calls to worker module's API.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'rubygems'
require 'rest_client'
require 'timeout'
require 'json'
require 'fileutils'
require 'yaml'

$conf = YAML::load File.read "#{File.dirname __FILE__}/../../common/config/conf.yml"
if $conf["master_use_swiftiply"]
  ENV["RAILS_ENV"] ||= "production"
else
  ENV["RAILS_ENV"] ||= "development"
end

require File.dirname(__FILE__) + "/../config/environment"

require File.dirname(__FILE__) + '/utils.rb'

# Fetch the body from RestClient reply result.
#
# Since::   0.3
def rep_body rep
  begin
    return rep.body
  rescue
    return rep
  end
end

# NSS module proxy.
#
# Since::   0.3
class NssProxy

  # The timeout to worker modules.
  #
  # Since::   0.3
  NSS_PROXY_TIMEOUT = 10

  # The status of the proxy.
  # Could be:
  # * failure: failed to conenct.
  # * running: the machine is up and running.
  #
  # Since::   0.3
  attr_reader :status

  # The most recent error message.
  #
  # Since::   0.3
  attr_reader :error_message

  # Create an NSS proxy.
  # * worker_addr: the address of the worker, in "ip:port" format, like "10.0.1.2:3000".
  #
  # Since::   0.3
  def initialize nss_addr
    @addr = nss_addr

    # The URL to the root ('/') of the worker.
    @root_url = "http://#{@addr}"

    @hostname = nil
    @version = nil
    @rails_env = nil

    timeout(NSS_PROXY_TIMEOUT) do
      begin
        begin
          raw_reply = rep_body(RestClient.get "#{@root_url}/misc/role.json")
          reply = JSON.parse raw_reply
          if reply["success"] != true or reply["message"] != "storage"
            @status = "failure"
            @error_message = "Failed to conenct '#{@addr}', raw reply is '#{raw_reply}'!"
          else
            @status = "running"
          end
        rescue Exception => e
          @status = 'failure'
          @error_message = "Exception occurred: '#{e.to_s}'"
        end
      rescue
        @status = "failure"
        @error_message = "Timeout connecting '#{@addr}'!"
      end
    end
  end

  # List the content of some dir. If 'dir' is not given, then by default, this function will list contents
  # of 'vdisks' folder. The 'dir' could be absolute path, or relative path to 'storage_root'.
  #
  # Since::   0.3
  def listdir dir = nil
    if dir
      ret = post_request "fs/listdir.json", :dir => dir
    else
      ret = get_request "fs/listdir.json"
    end
  end

  # Remove a file. The 'path' could be absolute path, or relative path to 'storage_root'.
  #
  # Since::   0.3
  def rm path
    ret = post_request "fs/rm.json", :path => path
  end

  # Move a file or directory. The path could be absolute path, or relative path to 'storage_root'.
  #
  # Since::   0.3
  def mv from_path, to_path
    ret = post_request "fs/mv.json", :from => from_path, :to => to_path
  end

  # Copy a file. The path could be absolute path, or relative path to 'storage_root'.
  #
  # Since::   0.3
  def cp from_path, to_path
    ret = post_request "fs/cp.json", :from => from_path, :to => to_path
  end

  # Get the role. The return value shoule be "storage"
  #
  # Since::   0.3
  def role
    ret = get_request "misc/role.json"
  end

  # Get the version of NSS module.
  #
  # Since::   0.3
  def version
    ret = get_request "misc/version.json"
  end

  # Get the hostname of NSS module.
  #
  # Since::   0.3
  def hostname
    ret = get_request "misc/hostname.json"
  end

  # Register a new vdisk.
  #
  # Since::   0.3
  def register_vdisk basename, pool_size
    ret = post_request "vdisk_pool/register.json", :basename => basename, :pool_size => pool_size
  end

  # Change the pool size of some vdisk image.
  #
  # Since::   0.3
  def edit_vdisk basename, pool_size
    ret = post_request "vdisk_pool/edit.json", :basename => basename, :pool_size => pool_size
  end

  # Unregister a image pool.
  #
  # Since::   0.3
  def unregister_vdisk basename
    ret = post_request "vdisk_pool/unregister.json", :basename => basename
  end


  # List all the vdisk image pool.
  #
  # Since::   0.3
  def list_vdisk
    ret = get_request "vdisk_pool/list.json"
  end

private

  # Send a POST request.
  # On exception return nil, otherwise return result data.
  #
  # Since::     0.3
  def post_request url, params = nil
    timeout(NSS_PROXY_TIMEOUT) do
      begin
        begin
          if params != nil
            raw_reply = rep_body(RestClient.post "#{@root_url}/#{url}", params)
          else
            raw_reply = rep_body(RestClient.post "#{@root_url}/#{url}")
          end
          @status = "running"
          reply = JSON.parse raw_reply
          return reply
        rescue Exception => e
          @error_message = "Exception occurred: '#{e.to_s}'"
          return nil
        end
      rescue
        @status = "failure"
        @error_message = "Timeout connecting '#{@addr}'!"
        return nil
      end
    end
  end

  # Send a GET request.
  # On exception return nil, otherwise return result data.
  #
  # Since::     0.3
  def get_request url
    timeout(NSS_PROXY_TIMEOUT) do
      begin
        begin
          raw_reply = rep_body(RestClient.get "#{@root_url}/#{url}")
          @status = "running"
          reply = JSON.parse raw_reply
          return reply
        rescue Exception => e
          @error_message = "Exception occurred: '#{e.to_s}'"
          return nil
        end
      rescue
        @status = "failure"
        @error_message = "Timeout connecting '#{@addr}'!"
        return nil
      end
    end
  end
end

