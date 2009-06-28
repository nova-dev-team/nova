## This controller handles the login/logout function of the site.  
class SessionsController < ApplicationController
  # Be sure to include AuthenticationSystem in Application Controller instead
  include AuthenticatedSystem

  # render new.rhtml
  def new
    if logged_in?
      redirect_to :controller => :home, :action => :index
    end
  end

  def create
    logout_keeping_session!
    user = User.authenticate(params[:login], params[:password])
    if user
      if user.activated ## check if the user is not activated
        # Protects against session fixation attacks, causes request forgery
        # protection if user resubmits an earlier form using back
        # button. Uncomment if you understand the tradeoffs.
        # reset_session
        self.current_user = user
        new_cookie_flag = (params[:remember_me] == "1")
        handle_remember_cookie! new_cookie_flag
        redirect_back_or_default('/')
        flash[:notice] = "Logged in successfully"
      else ## the user is activated
        note_failed_signin
        @login       = params[:login]
        @remember_me = params[:remember_me]
        params[:error_msg] = "Please wait for administrator's approval of '#{params[:login]}'"
        render :action => 'new'
      end
    else
      note_failed_signin
      @login       = params[:login]
      @remember_me = params[:remember_me]
      params[:error_msg] = "Failed login for '#{params[:login]}'"
      render :action => 'new'
    end
  end

  def destroy
    logout_killing_session!
    flash[:notice] = "You have been logged out."
    redirect_back_or_default('/')
  end

protected
  # Track failed login attempts
  def note_failed_signin
    flash[:error] = "Couldn't log you in as '#{params[:login]}'"
    logger.warn "Failed login for '#{params[:login]}' from #{request.remote_ip} at #{Time.now.utc}"
  end
end
