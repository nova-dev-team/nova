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
    if item == "img":
      img_map = {
        :hadoop => "hadoop_img.img",
        :mpi => "mpi_img.img"
      }
      value = img_map[value]
    end

    vcluster = Vcluster.find_by_id params[:id]  [1..-1]
    vcluster.vmachines.each do |vmachine|
      print "*** Change settings of v#{vmachine.id}"
      VmachineHelper::Helper.change_setting "v#{vmachine.id}", item, value 
    end
    vcluster.save

    render :text => "true"

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
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end
