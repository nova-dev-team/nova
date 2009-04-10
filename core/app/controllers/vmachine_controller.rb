class VmachineController < ApplicationController

  include VmachineHelper

  # list all the available virtual machines
  def list
    result = vmachine_list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # create a new virtual machine
  def create
    result = vmachine_create

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # delete a virtual machine, the vmachine cannot be under use
  def delete
    result = vmachine_delete params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # show detailed information about a virtual machine
  def info
    result = vmachine_info params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # start a vmachine, it should be already put on a pmachine
  def start
    result = vmachine_start params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # stop a vmachine, and also remove it from the hosting pmachine
  def stop
    result = vmachine_stop params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # suspend a virtual machine
  def suspend
    result = vmachine_suspend params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # resume a virtual machine
  def resume
    result = vmachine_resume params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # NOTE only to be called by pmachine
  def notify_status_change
    result = vmachine_notify_status_change params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end
