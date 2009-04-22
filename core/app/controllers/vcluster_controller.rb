class VclusterController < ApplicationController

  include VmachineHelper
  include UserHelper
  include VclusterHelper

  # list all the virtual clusters
  def list
    result = VclusterHelper::Helper.list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # create a new virtual cluster
  def create
    result = VclusterHelper::Helper.create params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # delete a vcluster, only when it is empty
  def delete
    result = VclusterHelper::Helper.delete params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # register a virtual machine to a virtual cluster
  # the virtual cluster must NOT be running when doing this (is it necessary?)
  def add_vmachine
    result = VclusterHelper::Helper.add_vmachine params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # remove a virtual machine from a virtual cluster
  # the virtual cluster must NOT be running when doing this (it will be checked)
  def remove_vmachine
    result = VclusterHelper::Helper.remove_vmachine params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # remove a vmachine from any vcluster
  def remove_vmachine_ex
    v = Vmachine.find_by_id params[:id][1..-1]
    result = VclusterHelper::Helper.remove_vmachine "c#{v.vcluster.id}", params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end
  
  # for front
  def info_vm_list
    r = VclusterHelper::Helper.info params[:id]
    result = []
    r[:vmachines].each { |vinfo|
      result << vinfo[:vid]
      if vinfo[:ip]
        result << vinfo[:ip]
      else
        result << "NOT DEPLOYED"
      end
      result << vinfo[:vimage_name]
      result << vinfo[:status]
    }

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # show detailed information about a virtual cluster
  def info
    result = VclusterHelper::Helper.info params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # create a vcluster and add to an user
  def create_and_add_to
    r = VclusterHelper::Helper.create params[:arg]
    r2 = UserHelper::Helper.add_vcluster params[:id], r[:vcluster_cid]
    result = {}
    result[:msg] = r[:msg] + "\n" + r2[:msg]
    if (r2[:success] and r[:success]) then
      result[:success] = true
    else
      result[:success] = false
    end
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end


  # create a new vmachine and add to this vcluster
  def add_new_vm
    r = VmachineHelper::Helper.create
    r2 = VclusterHelper::Helper.add_vmachine params[:id], r[:vmachine_vid]
    result = {}
    result[:msg] = r[:msg] + "\n" + r2[:msg]
    if (r[:success] and r2[:success]) then
      result[:success] = true
    else
      result[:success] = false
    end
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :jons => result}
    end
  end


end

