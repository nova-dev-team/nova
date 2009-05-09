class BatchController < ApplicationController

  include UserHelper
  include VmachineHelper
  include BatchHelper

  # create a batch and add to an user
  def create_and_add_to
    r = BatchHelper::Helper.create params[:cname], params[:csize]
    r2 = UserHelper::Helper.add_vcluster params[:id], r[:vcluster_cid]
    result = r[:vcluster_cid]
    respond_to do |accept|
      accept.html {render :text => result}
    end
  end

  def create
    result = BatchHelper::Helper.create params[:id], params[:arg]
    respond_to do |accept| 
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

=begin
  def list
    result = BatchHelper::Helper.list
    
    respond_to do |accept| 
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end
=end

  def change_setting
    # TODO

    item = params[:item]
    value = params[:value]

    vcluster = Vcluster.find_by_id params[:id][1..-1]

# the image on the 1st vmachine
    if item == "master_image"
      min_id = nil # master has minimum id within the cluster
      vcluster.vmachines.each do |vmachine|
        if min_id == nil or min_id > vmachine.id
          min_id = vmachine.id
        end
      end

      vcluster.vmachines.each do |vmachine|
        if vmachine.id == min_id
          VmachineHelper::Helper.change_setting "v#{vmachine.id}", "img", value
        end
      end

    elsif item == "slave_image"
      min_id = nil
      vcluster.vmachines.each do |vmachine|
        if min_id == nil or min_id > vmachine.id
          min_id = vmachine.id
        end
      end

      vcluster.vmachines.each do |vmachine|
        if vmachine.id != min_id
          VmachineHelper::Helper.change_setting "v#{vmachine.id}", "img", value
        end
      end

    else # other settings
      vcluster.vmachines.each do |vmachine|
        VmachineHelper::Helper.change_setting "v#{vmachine.id}", item, value 
      end
      vcluster.save

      render :text => "true"
    end

  end

  def add_soft
    result = BatchHelper::Helper.add_soft params[:id], params[:arg]
    
    respond_to do |accept| 
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def do_install
    result = BatchHelper::Helper.do_install params[:id]

    respond_to do |accept| 
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def progress
    result = BatchHelper::Helper.progress params[:id]
    respond_to do |accept| 
      accept.html {render :text => result}
      accept.json {render :json => result}
    end
  end

end

