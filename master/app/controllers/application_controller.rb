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

  # before_filter for privileged pages.
  # If user is visiting with browser, an redirect will be issued.
  # If the request is in JSON, error will be replied.
  #
  # Author::  Santa Zhang (santa1987@gmail.com)
  # Since::   0.3
  def login_required
    respond_to do |accept|
      accept.html { redirect_to login_url unless logged_in? }
      accept.json { reply_failure "Login required!" unless logged_in? }
    end
  end
  
end
