class UsersController < ApplicationController

  # render new.rhtml
  def new
    @user = User.new
  end
 
  def create
    logout_keeping_session!
    @user = User.new(params[:user])
    params[:user][:groups].split.each {|g_name| @user.groups << (Group.find_by_name g_name)}
    success = @user && @user.save
    if success && @user.errors.empty?
            # Protects against session fixation attacks, causes request forgery
      # protection if visitor resubmits an earlier form using back
      # button. Uncomment if you understand the tradeoffs.
      # reset session

      ## don't sign in the user immediately, since we need to activate it by admin
      # self.current_user = @user # !! now logged in

      redirect_back_or_default('/')
      flash[:notice] = "Thanks for signing up!  We're sending you an email with your activation code."
    else
      flash[:error]  = "We couldn't set up that account, sorry.  Please try again, or contact an admin (link is above)."
      render :action => 'new'
    end
  end

  def update
    root_or_admin_required
    require 'pp'
    pp params
    if params[:activated]
      u = User.find_by_id params[:id]
      u.activated = (params[:activated] == 'true')
      pp u.save
    end
    respond_to do |accept|
      accept.json {
        render :text => "hi".to_json
      }
    end
  end

private

  def root_or_admin_required
    redirect_to login_url unless logged_in? and (current_user.in_group? "admin" or current_user.in_group? "root")
  end

end
