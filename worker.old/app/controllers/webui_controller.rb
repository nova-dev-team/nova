class WebuiController < ApplicationController

  def index
    render :template => 'webui/index.html.erb', :layout => 'default'
  end

end
