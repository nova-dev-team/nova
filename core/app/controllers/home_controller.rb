class HomeController < ApplicationController
  
  before_filter :login_required

  # enable index.html
  def index
  end

private

  def login_required
    redirect_to login_url unless logged_in?
  end

end
