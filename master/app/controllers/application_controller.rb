# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
  ## TODO find a way to replace protect_from_forgery
  # protect_from_forgery # See ActionController::RequestForgeryProtection for details

  # Scrub sensitive parameters from your log
  # filter_parameter_logging :password

  # Be sure to include AuthenticationSystem in Application Controller instead
  include AuthenticatedSystem
  


protected
  ## TODO add some helper functions
  
  
  def redirect_unless_logged_in
    redirect_to login_url unless logged_in?
  end
  
  def render_result success, message, option = {}
    result = {:success => success, :message => message}.merge(option)
    respond_to do |accept|
      accept.json {render :json => result}
      accept.html {render :text => result.to_json}
    end    
  end
  
  def render_failure message, option = {}
    render_result false, message, option
  end
  
  def render_success message, option = {}
    render_result true, message, option
  end
  
  def logged_in_and_activated?
    logged_in? and current_user.activated?
  end

end
