# Controller for virtual clusters
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'utils'
require 'uuidtools'

class VclustersController < ApplicationController

  before_filter :login_required

  # List all the clusters visible for current user.
  # root and admin could see all clusters, while users can only see his own clusters.
  #
  # Since::     0.3
  def list
    if @current_user.privilege == "normal_user"
      vcluster_list = @current_user.vclusters
    else
      vcluster_list = Vcluster.all
    end
    vcluster_info = vcluster_list.collect do |vc|
      {
        "name" => vc.cluster_name,
        "first_ip" => vc.first_ip,
        "size" => vc.cluster_size
      }
    end
    reply_success "query successful!", :data => vcluster_info
  end

  # Create a new virtual cluster with given cluster size.
  # Will reply failure if cannot create such a big cluster.
  #
  # TODO provide more detailed error info
  #
  # Since::     0.3
  def create
    unless valid_param? params[:name] and valid_param? params[:size] and params[:size].to_i.to_s == params[:size] and params[:size].to_i > 0 and valid_param? params[:machines]
      reply_failure "Please provide valid 'name', 'size' and 'machines' parameter!"
      return
    end
    ret = Vcluster.alloc_cluster params[:name], params[:size].to_i, @current_user
    if ret[:success] == true
      vc = ret[:vcluster]

      # create vmachines
      vm_counter = 0
      begin
        vm_info = {:name => nil, :vdisk_fname => nil, :cpu_count => nil, :mem_size => nil, :soft_list => nil}
        params[:machines].each_line do |line|
          line = line.strip
          if line == "" and vm_info[:name] != nil
            vm = Vmachine.new
            vm.name = vc.cluster_name + "-" + vm_info[:name]
            vm.uuid = UUIDTools::UUID.random_create.to_s
            vm.cpu_count = vm_info[:cpu_count].to_i
            vm.memory_size = vm_info[:mem_size].to_i
            vm.soft_list = vm_info[:soft_list]
            vm.hda = vm_info[:vdisk_fname]
            vm.ip = IpTools.i_to_ipv4(IpTools.ipv4_to_i(vc.first_ip) + vm_counter)

            vm.vcluster = vc
            vc.vmachines << vm

            raise "Failed to create vmachine!" unless vm.save
            vm_counter += 1

            vm_info = {:name => nil, :vdisk_fname => nil, :cpu_count => nil, :mem_size => nil, :soft_list => nil}
          else
            case line
            when /^vdisk_fname=/
              vm_info[:vdisk_fname] = line[12..-1]
            when /^machine_name=/
              vm_info[:name] = line[13..-1]
            when /^cpu_count=/
              vm_info[:cpu_count] = line[10..-1]
            when /^mem_size=/
              vm_info[:mem_size] = line[9..-1]
            when /^soft_list=/
              vm_info[:soft_list] = line[10..-1]
            else
              raise "Wrong parameter for 'machines'!"
            end
          end
        end
      rescue Exception => e
        # revert the creation of vcluster
        Vcluster.delete vc rescue nil
        reply_failure "Exception occurred! Message: '#{e.to_s}'. The cluster is not created!"
        return
      end

      reply_success ret[:message]
    else
      reply_failure ret[:message]
    end
  end

  # Display detail info about a cluster.
  #
  # Since::     0.3
  def show
    unless valid_param? params[:name]
      reply_failure "Please provide valid 'name' parameter!"
      return
    end
    vc = Vcluster.find_by_cluster_name params[:name]
    if vc
      if @current_user.privilege != "root" and (@current_user.vclusters.include? vc) == false
        reply_failure "You are not allowed to do this!"
        return
      end
      machines_info = []
      vc.vmachines.each do |vm|
        machines_info << {
          :id => vm.id,
          :name => vm.hostname,
          :uuid => vm.uuid,
          :cpu_count => vm.cpu_count,
          :mem_size => vm.memory_size,
          :disk_image => (Vdisk.find_by_file_name vm.hda).display_name,
          :soft_list => vm.soft_list,
          :status => vm.status
        }
      end

      reply_success "Query successful!",
        :name => vc.cluster_name,
        :size => vc.cluster_size,
        :owner => vc.user.login,
        :first_ip => vc.first_ip,
        :last_ip => (IpTools.i_to_ipv4(IpTools.ipv4_to_i(vc.first_ip) + vc.cluster_size - 1)),
        :machines => machines_info
    else
      reply_failure "Cannot find vcluster with name '#{params[:name]}'!"
    end
  end

  # Start all VM inside a cluster.
  #
  # Since::     0.3.1
  def start_all_vm
    unless valid_param? params[:name]
      reply_failure "Please provide valid 'name' parameter!"
      return
    end
    vc = Vcluster.find_by_cluster_name params[:name]
    if vc
      if @current_user.privilege != "root" and (@current_user.vclusters.include? vc) == false
        reply_failure "You are not allowed to do this!"
        return
      end
      vc.vmachines.each do |vm|
        if vm.status == "shut-off" or vm.status == "boot-failure"
          vm.status = "start-pending"
          vm.save
        end
      end
      reply_success "Starting all VM in vcluster with name '#{params[:name]}'."
    else
      reply_failure "Cannot find vcluster with name '#{params[:name]}'!"
    end
  end

  # Stop all VM inside a cluster.
  #
  # Since::     0.3.1
  def stop_all_vm
    unless valid_param? params[:name]
      reply_failure "Please provide valid 'name' parameter!"
      return
    end
    vc = Vcluster.find_by_cluster_name params[:name]
    if vc
      if @current_user.privilege != "root" and (@current_user.vclusters.include? vc) == false
        reply_failure "You are not allowed to do this!"
        return
      end
      vc.vmachines.each do |vm|
        if vm.status == "running" or vm.status == "suspended" or vm.status == "start-preparing"
          vm.status = "shutdown-pending"
          vm.save
        elsif vm.status == "boot-failure"
          vm.status = "shut-off"
          vm.save
        end
      end
      reply_success "Shutting down all VM in vcluster with name '#{params[:name]}'."
    else
      reply_failure "Cannot find vcluster with name '#{params[:name]}'!"
    end
  end

  # Destroy a cluster.
  #
  # Since::     0.3
  def destroy
    unless valid_param? params[:name]
      reply_failure "Please provide valid 'name' parameter!"
      return
    end
    vc = Vcluster.find_by_cluster_name params[:name]
    if vc
      if @current_user.privilege != "root" and (@current_user.vclusters.include? vc) == false
        reply_failure "You are not allowed to do this!"
        return
      end
      # only allowed to destroy, when all machine is shut-down
      vc.vmachines.each do |vm|
        if vm.status != "shut-off"
          reply_failure "You cannot destroy a cluster unless all machines are 'shut-off'!"
          return
        end
      end
      vc.vmachines.each do |vm|
        Vmachine.delete vm
      end
      # TODO notify the background daemon to destroy vmachines
      Vcluster.delete vc
      reply_success "Destroyed vcluster with name '#{params[:name]}'."
    else
      reply_failure "Cannot find vcluster with name '#{params[:name]}'!"
    end
  end

end

