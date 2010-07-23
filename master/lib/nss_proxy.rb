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
require 'utils'

$conf = YAML::load File.read "#{File.dirname __FILE__}/../../common/config/conf.yml"
if $conf["master_use_swiftiply"]
  ENV["RAILS_ENV"] ||= "production"
else
  ENV["RAILS_ENV"] ||= "development"
end

require File.dirname(__FILE__) + "/../config/environment"


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
          raw_reply = (RestClient.get "#{@root_url}/misc/role.json").body
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

  def listdir dir = nil
    if dir
      ret = post_request "fs/listdir.json", :dir => dir
    else
      ret = get_request "fs/listdir.json"
    end
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
            raw_reply = (RestClient.post "#{@root_url}/#{url}", params).body
          else
            raw_reply = (RestClient.post "#{@root_url}/#{url}").body
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
          raw_reply = (RestClient.get "#{@root_url}/#{url}").body
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

