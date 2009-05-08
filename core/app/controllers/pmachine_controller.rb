class PmachineController < ApplicationController

  include PmachineHelper

  # list all the physical machines
  def list
    result = Helper.list

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # only list the working pmachines
  def list_working
    result = {:success => true}
    pm_list = []
    Pmachine.all.each do |pm|
      if pm.status == "working":
        pm_list << { :pm_ip => pm.ip }
      end
    end
    result[:pm_list] = pm_list
    
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # also gives the working status
  def list_ex
    result = {:success => true}
    pm_info = []
    Pmachine.all.each do |pm|
      pm_info << {
        :pm_ip => pm.ip,
        :is_working => pm.status
      }
    end
    result[:pm_info] = pm_info
    
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # add a new physical machine by ip address, eg: /pmachine/add/10.0.0.1
  def add
    result = Helper.add params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def mark_remove
    result = Helper.mark_remove params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def unmark_remove
    result = Helper.unmark_remove params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

=begin These 2 methods are not supposed to be used. vmachine hosting is done by the 'vmachine.start' function
  # host a vmachine to a pmachine, eg: /pmachine/host_vmachine/10.0.2.3/v123
  def host_vmachine
    result = PmachineWorker.host_vmachine params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  # unhost a vmachine from a pmachine
  def unhost_vmachine
    result = PmachineWorker.unhost_vmachine params[:id], params[:arg]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end
=end

  # information about a physical machine
  def info
    result = Helper.info params[:id]

    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end
