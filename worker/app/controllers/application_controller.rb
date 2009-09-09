# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
#  protect_from_forgery # See ActionController::RequestForgeryProtection for details

  # Scrub sensitive parameters from your log
  # filter_parameter_logging :password

protected

  def render_success message
    render_result :success => true, :message => message
  end

  def render_failure message
    render_result :success => false, :message => message
  end

  # reply to client
  def render_result result
    logger.debug "*** [reply] success=#{result[:success]}, message=#{result[:message]}" if result[:success] and result[:message]
    respond_to do |accept|
      accept.json {render :json => result}
      accept.html {render :text => result.pretty_inspect}
    end
  end

end

