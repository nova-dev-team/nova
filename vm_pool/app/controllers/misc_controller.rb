# A helper controller that does many things.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'utils'
require 'yaml'
require 'fileutils'
require 'rest_client'

class MiscController < ApplicationController

  # Reply the role of this module.
  #
  # Since::     0.3
  def role
    reply_success "vm_pool"
  end

  # Use the 'port_mapper' tool to add a port forwarding.
  #
  # Since::     0.3
  def add_port_mapping
    return unless valid_ip_and_port_param?

    timeout = nil
    if valid_param? params[:timeout]
      if params[:timeout].to_i.to_s != params[:timeout]
        reply_failure "Please provide a valid 'timeout'!"
        return
      end
      timeout = params[:timeout]
    end

    local_port = nil
    if valid_param? params[:local_port]
      if params[:local_port].to_i.to_s != params[:local_port]
        reply_failure "Please provide a valid 'local_port'!"
        return
      end
      local_port = params[:local_port].to_i
    end

    fwd_addr = "#{params[:ip]}:#{params["port"]}"
    # check if already has got a port forwarding
    port_file = "#{RAILS_ROOT}/log/#{fwd_addr.gsub ":", "_"}.local_port"
    if File.exists? port_file
      local_port = File.read(port_file).to_i
      FileUtils.touch port_file  # mark the file new, so that the daemon will not time out
    else
      if local_port == nil
        local_port = 12000 + rand(36000)  # random port
      end
      if timeout == nil
        if valid_param? params[:timeout] and params[:timeout].to_i.to_s == params[:timeout]
          timeout = params[:timeout].to_i
        else
          timeout = 0 # forever working
        end
      end
      my_exec "#{RAILS_ROOT}/lib/port_fwd_daemon #{timeout} #{local_port} #{fwd_addr} #{RAILS_ROOT}/log #{RAILS_ROOT}/../tools/server_side/bin/port_mapper"
    end
    reply_success "mapping added", :port => local_port
  end

  # List all the available port mappings.
  #
  # Since::     0.3
  def list_port_mapping
    mappings = []
    Dir.foreach("#{RAILS_ROOT}/log") do |entry|
      next unless entry =~ /\.pid$/
      pid = File.read("#{RAILS_ROOT}/log/#{entry}").to_i
      begin
        Process.kill 0, pid
      rescue
        next
      end
      main_fn = entry[0..-5]
      next unless main_fn =~ /^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+_[0-9]+$/
      port_fn = File.join "#{RAILS_ROOT}/log/#{main_fn}.local_port"
      if File.exists? port_fn
        mappings << {
          :local_port => File.read(port_fn).to_i,
          :port => main_fn.split("_")[1].to_i,
          :ip => main_fn.split("_")[0]
        }
      else
        # kill broken mappings
        kill_by_pid_file "#{RAILS_ROOT}/log/#{entry}"
      end
      
    end
    reply_success "query successful", :data => mappings
  end

  # Remove an existing port mapping.
  #
  # Since::     0.3
  def del_port_mapping
    return unless valid_ip_and_port_param?
    fwd_addr = "#{params[:ip]}:#{params["port"]}"
    port_file = "#{RAILS_ROOT}/log/#{fwd_addr.gsub ":", "_"}.local_port"
    pid_file = "#{RAILS_ROOT}/log/#{fwd_addr.gsub ":", "_"}.pid"

    if File.exists? pid_file
      kill_by_pid_file pid_file
      reply_success "The mapping is deleted."
    else
      reply_failure "The port mapping was not found."
    end

    if File.exists? port_file
      FileUtils.rm port_file
    end
  end

  # Acquire a VM from the pool. If name is given, it tries to acquire that vm. If name not given, it selects a vm from the pool.
  #
  # Since::     0.3
  def acquire
    if valid_param? params[:name]
      # name given, try to get the vm from pool
      vm = Vmachine.find_by_name params[:name]
      if vm == nil
        reply_failure "VM with name='#{params[:name]}' not found!"
      elsif vm.using or vm.status.downcase != "running"
        reply_failure "VM with name='#{params[:name]}' is already used!"
      else
        vm.using = true
        vm.save
        reply_success "successfully acquired VM", :name => vm.name
      end
    else
      # name not given, select a vm from pool
      Vmachine.all.each do |vm|
        if vm.using == false and vm.status.downcase == "running"
          vm.using = true
          vm.save
          reply_success "successfully acquired VM", :name => vm.name
          return
        end
      end
      reply_failure "failed to acquire new VM"
    end
  end

  # Release an acquired VM back into the pool.
  #
  # Since::     0.3
  def release
    unless valid_param? params[:name]
      reply_failure "Please provide the 'name' parameter!"
      return
    end
    vm = Vmachine.find_by_name params[:name]
    if vm == nil
      reply_failure "VM with name '#{vm.name}' not found!"
      return
    end
    if vm.using == false
      reply_failure "VM '#{vm.name}' is not being used!"
      return
    end
    vm.using = false
    vm.use_count += 1
    vm.save
    reply_success "VM '#{vm.name}' released, current use_count = #{vm.use_count}"
  end

  # Destroy vmachine.
  #
  # Since::     0.3
  def destroy_vm
    unless valid_param? params[:name]
      reply_failure "Please provide the 'name' parameter!"
      return
    end
    vm = Vmachine.find_by_name params[:name]
    if vm == nil
      reply_failure "VM with name '#{vm.name}' not found!"
      return
    end
    Vmachine.delete vm
    conf = YAML::load File.read "#{RAILS_ROOT}/../common/config/conf.yml"
    pm_addr = "http://#{vm.pmachine.ip}:#{conf["worker_port"]}"
    RestClient.post "#{pm_addr}/vmachines/destroy.json", :name => params[:name]
    reply_success "VM with name='#{params[:name]}' destoryed"
  end

  # List info of all the vmachines.
  #
  # Since::     0.3
  def list_vm
    reply_model Vmachine, :items => ["id", "name", "use_count", "uuid", "using", "pmachine_id", "status"]
  end

private

  def valid_ip_and_port_param?
    unless valid_param? params[:ip] and params[:ip].is_ip_addr?
      reply_failure "Please provide a valid 'ip'."
      return false
    end
    unless valid_param? params[:port] and params[:port].to_i.to_s == params[:port]
      reply_failure "Please provide a valid 'port'."
      return false
    end
    return true
  end
  
end
