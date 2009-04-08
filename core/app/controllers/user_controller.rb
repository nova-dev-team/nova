class UserController < ApplicationController

  include UserHelper

  # show all the users' id (email)
  def list
    result = UserWorker.list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end

  end

  # add a new user
  def add
    result = UserWorker.add params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end

  end

  # show a certain user's detailed information
  def info
    result = UserWorker.info params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end

  end

  # add a virtual cluster to a user. the vcluster is already created
  def add_vcluster
    result = UserWorker.add_vcluster params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end


  # remove a virtual cluster from a user
  def remove_vcluster
    result = UserWorker.remove_vcluster params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end

