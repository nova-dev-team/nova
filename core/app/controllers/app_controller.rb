class AppController < ApplicationController
  
  before_filter :login_required

  # enable index.html
  def home
    render :template => 'app/home.html.erb', :layout => 'default'
  end

private

  def login_required
    redirect_to login_url unless logged_in?
  end

end
