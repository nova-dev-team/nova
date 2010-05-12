# Controller for virtual clusters
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require 'utils'

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
  # Since::     0.3
  def create
    unless valid_param? params[:name] and valid_param? params[:size] and params[:size].to_i.to_s == params[:size] and params[:size].to_i > 0
      reply_failure "Please provide valid 'name' and 'size' parameter!"
      return
    end
    ret = Vcluster.alloc_cluster params[:name], params[:size].to_i, @current_user
    if ret[:success] == true
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
      reply_success "Query successful!",
        :name => vc.cluster_name,
        :size => vc.cluster_size,
        :first_ip => vc.first_ip,
        :last_ip => (IpTools.i_to_ipv4(IpTools.ipv4_to_i(vc.first_ip) + vc.cluster_size - 1))
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
      Vcluster.delete vc
      reply_success "Destroyed vcluster with name '#{params[:name]}'."
    else
      reply_failure "Cannot find vcluster with name '#{params[:name]}'!"
    end
  end

end

