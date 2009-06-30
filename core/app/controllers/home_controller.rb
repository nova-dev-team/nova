class HomeController < ApplicationController
  
  before_filter :login_required

  # enable index.html
  def index
    render :template => 'home/user.html.erb', :layout => 'default'
  end

private

  def login_required
    redirect_to login_url unless logged_in?
  end

end
