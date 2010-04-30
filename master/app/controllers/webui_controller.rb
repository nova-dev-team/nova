# Controller for plain web ui.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class WebuiController < ApplicationController

  before_filter :login_required
  layout "webui"
 
  # A tricky handler, immediately redirects to a full url. This prevents broken relative address.
  #
  # Since::   0.3
  def index
    if @current_user.privilege == "normal user"
      redirect_to :controller => :webui, :action => :workspace
    else
      redirect_to :controller => :webui, :action => :overview
    end
  end

end

