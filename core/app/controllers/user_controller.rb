class UserController < ApplicationController

  include UserHelper

  # show all the users' id (email)
  def list
    result = Helper.list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end

  end
  
  

  # add a new user
  def add
    result = Helper.add params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end

  end

  # show a certain user's detailed information
  def info
    result = Helper.info params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end

  end
  
  
  # port for front
  def info_vclusters
    r = Helper.info params[:id]
    result = []
    r[:vclusters].each { |clu|
      result << clu[:cid];
      result << clu[:name];
    }
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # add a virtual cluster to a user. the vcluster is already created
  def add_vcluster
    result = Helper.add_vcluster params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end


  # remove a virtual cluster from a user
  def remove_vcluster
    result = Helper.remove_vcluster params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end

