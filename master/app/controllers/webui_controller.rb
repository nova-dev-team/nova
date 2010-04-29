# Controller for plain web ui.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class WebuiController < ApplicationController

  before_filter :login_required
 
  # A tricky handler, immediately redirects to a full url. This prevents broken relative address.
  #
  # Since::   0.3
  def index
    redirect_to :controller => :webui, :action => :workspace
  end

  def workspace
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def wizard
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def user
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def worker
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def resource
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def setting
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def monitor
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

  def account
    respond_to do |accept|
      accept.html { render :layout => "layouts/webui" }
    end
  end

end

