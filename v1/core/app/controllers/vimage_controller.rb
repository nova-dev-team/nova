class VimageController < ApplicationController

  include VimageHelper

  def list
    result = Helper.list
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # for front
  def short_list
    result = Helper.short_list
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def add
    result = Helper.add params[:os_family], params[:os_name], params[:location], params[:comment]
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def hide
    result = Helper.hide params[:id]
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def unhide
    result = Helper.unhide params[:id]
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end

