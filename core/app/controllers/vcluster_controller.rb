class VclusterController < ApplicationController

  include VclusterHelper

  # list all the virtual clusters
  def list
    result = VclusterWorker.list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # create a new virtual cluster
  def create
    result = VclusterWorker.create

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # delete a vcluster, only when it is empty
  def delete 
    result = VclusterWorker.delete params[:id]

    repsond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # register a virtual machine to a virtual cluster
  # the virtual cluster must NOT be running when doing this (is it necessary?)
  def add_vmachine
    result = VclusterWorker.add_vmachine params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # remove a virtual machine from a virtual cluster
  # the virtual cluster must NOT be running when doing this (it will be checked)
  def remove_vmachine
    result = VclusterWorker.remove_vmachine params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # show detailed information about a virtual cluster
  def info
    result = VclusterWorker.info params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end

