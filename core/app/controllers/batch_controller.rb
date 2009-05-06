class BatchController < ApplicationController

  include BatchHelper

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
