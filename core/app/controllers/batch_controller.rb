class BatchController < ApplicationController

  include UserHelper
  include BatchHelper

  # create a batch and add to an user
  def create_and_add_to
    r = BatchHelper::Helper.create params[:cname], params[:csize]
    r2 = UserHelper::Helper.add_vcluster params[:id], r[:vcluster_cid]
    result = {}
    result[:msg] = r2[:msg]
    result[:vcluster_cid] = r[:vcluster_cid]
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
