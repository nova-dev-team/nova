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

  include ControllerUtility

protected

  # Filter to check if user logged in, and if current user is "root".
  # Only root could view/change system settings.
  # If the user is root, returns true. Other wise false is returned and error message is replied.
  #
  # Since::   0.3
  def root_required
    unless logged_in? and @current_user.privilege == "root"
      reply_failure "You do not have enough privilege for this action!"
      return false
    else
      return true
    end
  end

  # Filter to check if user logged in, and if current user is "root" or "admin".
  #
  # Since::   0.3
  def root_or_admin_required
    unless logged_in? and (@current_user.privilege == "root" or @current_user.privilege == "admin")
      reply_failure "You do not have enough privilege for this action!"
      return false
    else
      return true
    end
  end

  # Fileter to check if user logged in , and if current user is "root" or "admin".
  # If current user is root or admin, returns true. Otherwise false is returned and error message is replied.
  #
  # Since::   0.3
  def root_or_admin_required
    unless logged_in? and (@current_user.privilege == "root" or @current_user.privilege == "admin")
      reply_failure "You do not have enough privilege for this action!"
      return false
    else
      return true
    end
  end

  # Check if all params are provided.
  # 'params' is provided as a string, separated by spaces.
  #
  # Since::   0.3
  def params_required params_str
    params_str.split.each do |par|
      unless valid_param? params[par]
        reply_failure "Please provide '#{par}' parameter!"
        return false
      end
    end
    return true
  end

end
