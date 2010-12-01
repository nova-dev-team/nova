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

# Worker module proxy.
#
# Since::   0.3
class WorkerProxy

  # The timeout to worker modules.
  #
  # Since::   0.3
  WORKER_PROXY_TIMEOUT = 10

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

  # Create a worker proxy.
  # * worker_addr: the address of the worker, in "ip:port" format, like "10.0.1.2:3000".
  #
  # Since::   0.3
  def initialize worker_addr
    @addr = worker_addr

    # The URL to the root ('/') of the worker.
    @root_url = "http://#{@addr}"

    @hostname = nil
    @version = nil
    @rails_env = nil

    timeout(WORKER_PROXY_TIMEOUT) do
      begin
        begin
          raw_reply = rep_body(RestClient.get "#{@root_url}/misc/role.json")
          reply = JSON.parse raw_reply.to_s
          if reply["success"] != true or reply["message"] != "worker"
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

  # Get a list of all the running VMs.
  # On error return nil.
  #
  # Since::   0.3
  def list_vm
    ret = get_request "vmachines/index.json"
    if ret
      vm_list = ret["data"]

      # make sure the 'status' value is consistent with db model
      # possible status: "running", "failure", "saving", "preparing", "suspended", "not_running"
      vm_list.each do |vm|
        vm["status"] = vm["status"].downcase
        vm["status"] = vm["status"].gsub " ", "_"
      end
      return vm_list
    else
      # error occured, return nil
      return nil
    end
  end

  # Start a vm.
  # Returns raw result. On error return nil.
  #
  # Required params:
  #  * cpu_count:       Number of CPUs
  #  * memory_size:     Memory size in MB
  #  * name:            Machine name
  #  * uuid:            Machine UUID
  #  * use_hvm:         Should hvm be used?
  #  * vdisk_fname:     Disk image file name
  #  * ip:              IP address.
  #  * submask:         Subnet mask.
  #  * gateway:         Net gateway
  #  * dns:             DNS server
  #  * packages:        List of packages, comma separated, in a string line.
  #  * nodelist:        List of the nodes. Each line is "ip hostname".
  #  * cluster_name:    Name of the cluster.
  #
  # Since::   0.3
  def start_vm params
    real_params = {
      :hypervisor => $conf["hypervisor"],
      :arch => "i686",
      :name => params[:name],
      :uuid => params[:uuid],
      :use_hvm => params[:use_hvm],
      :mem_size => params[:memory_size],
      :cpu_count => params[:cpu_count],
      :hda_image => params[:vdisk_fname],
      :run_agent => true,
      :agent_hint => <<AGENT_HINT
ip=#{params[:ip]}
subnet_mask=#{params[:submask]}
gateway=#{params[:gateway]}
dns=#{params[:dns]}
agent_packages=#{params[:packages]}
nodelist=#{params[:nodelist]}
cluster_name=#{params[:cluster_name]}
id_rsa.pub=#{File.read("#{ENV["HOME"]}/.ssh/id_rsa.pub").chomp rescue ""}
id_rsa=#{(File.read("#{ENV["HOME"]}/.ssh/id_rsa").strip.delete "\r\n").strip rescue ""}
AGENT_HINT
    }
    [:kernel, :initrd, :hda_dev].each do |item|
      if params.keys.include? item
        real_params[item] = params[item]
      end
    end
    real_params[:agent_hint] = real_params[:agent_hint].strip
    post_request "vmachines/start.json", real_params
  end

  # Suspend a running vm.
  # Returns raw result. On error return nil.
  #
  # Since::   0.3
  def suspend_vm name
    post_request "vmachines/suspend.json", :name => name
  end

  # Resume a suspended vm.
  #
  # Since::   0.3
  def resume_vm name
    post_request "vmachines/resume.json", :name => name
  end

  # Destroy a running vm.
  #
  # Since::   0.3
  def destroy_vm name
    post_request "vmachines/destroy.json", :name => name
  end

  # Power off a running vm.
  #
  # Since::   0.3.5
  def power_off_vm name
    post_request "vmachines/power_off.json", :name => name
  end

  # Live migration.
  #
  # Since::   0.3
  def live_migrate vm_name, dest_ip
    post_request "vmachines/live_migrate_to.json", :name => vm_name, :migrate_dest => dest_ip
  end

  # Get the hostname of target machine.
  # Return nil on error.
  #
  # Since::   0.3
  def get_hostname
    return @hostname if @hostname != nil
    ret = get_request 'misc/hostname.json'
    return nil if ret == nil or ret["success"] = false
    @hostname = ret["hostname"]
    return @hostname
  end

  # Get the version of target machine.
  # Return nil on failure.
  #
  # Since::   0.3
  def get_version
    return @version if @version != nil
    ret = get_request 'misc/version.json'
    return nil if ret == nil or ret["success"] = false
    @version = ret["version"]
    return @version
  end

  # Get the rails_env of target machine.
  # Return nil on failure.
  #
  # Since::   0.3
  def get_rails_env
    return @rails_env if @rails_env != nil
    ret = get_request 'misc/rails_env.json'
    return nil if ret == nil or ret["success"] = false
    @rails_env = ret["env"]
    return @rails_env
  end

  # Revoke an vm image on the worker machine.
  # Return {success, message} pair.
  #
  # Since::   0.3
  def revoke_image image_name
    ret = post_request "misc/revoke_vm_image.json", :image_name => image_name
    return ret if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

  # Revoke an package on the worker machine.
  # Return {success, message} pair.
  #
  # Since::   0.3
  def revoke_package pkg_name
    ret = post_request "misc/revoke_package.json", :package_name => pkg_name
    return ret if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

  # Get a list of all the settings.
  # On error return nil.
  #
  # Since::   0.3
  def list_setting
    ret = get_request "settings/index.json"
    return ret["data"] if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

  # Show a specific setting. On error return nil.
  #
  # Since::   0.3
  def show_setting key
    ret = post_request "settings/show.json", :key => key
    return ret["value"] if ret != nil
    return nil
  end

  # Edit a specific setting.
  # Return {success, message} pair. Actually, if no exception occurred, the original reply will be returned.
  #
  # Since::   0.3
  def edit_setting key, value
    ret = post_request "settings/edit.json", :key => key, :value => value
    return ret if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

  def suspend_all
    ret = get_request "vmachines/suspend_all.json"
    return ret if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

  def resume_all
    ret = get_request "vmachines/resume_all.json"
    return ret if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

  def hotbackup_to vm_name, slave_ip
    ret = post_request "vmachines/hotbackup_to.json", :name => vm_name, :hotbackup_dest => slave_ip
    return ret if ret != nil
    return {:success => false, :message => "Request to worker module failed!"}
  end

private

  # Send a POST request.
  # On exception return nil, otherwise return result data.
  #
  # Since::     0.3
  def post_request url, params = nil
    timeout(WORKER_PROXY_TIMEOUT) do
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
    timeout(WORKER_PROXY_TIMEOUT) do
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

