# This controller handles the login/logout action.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class SessionsController < ApplicationController

  # If user logged in, redirects to home. Other wise render the login page.
  #
  # Since::   0.3
  def new
    if logged_in?
      redirect_to home_url
    end
  end

  # Handles login request.
  #
  # Since::   0.3
  def create
    logout_keeping_session!
    user = User.authenticate(params[:login], params[:password])
    if user
      # Check if user is activated
      if user.activated
        # Protects against session fixation attacks, causes request forgery
        # protection if user resubmits an earlier form using back
        # button. Uncomment if you understand the tradeoffs.
        # reset_session
        self.current_user = user
        #new_cookie_flag = (params[:remember_me] == "1")
        #handle_remember_cookie! new_cookie_flag

        respond_to do |accept|
          accept.html { redirect_back_or_default("/") }
          accept.json { reply_success "You are now logged in." }
        end
      else
        # User not activated
        note_failed_signin
        #@login       = params[:login]
        #@remember_me = params[:remember_me]
        respond_to do |accept|
          accept.json { reply_failure "'#{params[:login]}' is not activated yet!" }
        end
      end
    else
      note_failed_signin
      #@login       = params[:login]
      #@remember_me = params[:remember_me]
      respond_to do |accept|
        accept.json { reply_failure "Incorrect login info for '#{params[:login]}', check your user id and password!" }
      end
    end
  end

  # Logout current user.
  #
  # Since::   0.3
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
