class VmachineController < ApplicationController

  require 'pp'

  include VmachineHelper

  # list all the available virtual machines
  def list
    result = Helper.list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # create a new virtual machine
  def create
    result = Helper.create
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # delete a virtual machine, the vmachine cannot be under use
  def delete
    result = Helper.delete params[:id]
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # show detailed information about a virtual machine
  def info
    result = Helper.info params[:id]
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # start a vmachine, it should be already put on a pmachine
  def start
    result = Helper.start params[:id]
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # stop a vmachine, and also remove it from the hosting pmachine
  def stop
    result = Helper.stop params[:id]
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # suspend a virtual machine
  def suspend
    result = Helper.suspend params[:id]
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # resume a virtual machine
  def resume
    result = Helper.resume params[:id]
    pp result
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end


=begin This function is internally initiated by core
  def notify_status_change
    result = Helper.notify_status_change params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end
=end

end
