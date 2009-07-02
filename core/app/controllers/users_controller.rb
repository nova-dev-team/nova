require 'pp'

class UsersController < ApplicationController

  # render new.rhtml
  def new
    @user = User.new
  end
 
  def create
    logout_keeping_session!
    @user = User.new(params[:user])
    ## TODO ADD to user group @user.groups << (Group.find_by_name 'user') ## default user group
    success = @user && @user.save
    if success && @user.errors.empty?
            # Protects against session fixation attacks, causes request forgery
      # protection if visitor resubmits an earlier form using back
      # button. Uncomment if you understand the tradeoffs.
      # reset session

      ## don't sign in the user immediately, since we need to activate it by admin
      # self.current_user = @user # !! now logged in
      
      respond_to do |accept|
        accept.json {
          render :json => {:success => true, :message => "Your account has been registered. Please wait for administrator's approval."}
        }
      end
      
      ## redirect_back_or_default('/')
      ## flash[:notice] = "Thanks for signing up!  We're sending you an email with your activation code."
    else
      ## flash[:error]  = "We couldn't set up that account, sorry.  Please try again, or contact an admin (link is above)."
      ## render :action => 'new'
      pp @user.errors
      error_text = ""
      @user.errors.each {|item, reason| error_text += item + " ==> " + reason}
      respond_to do |accept|
        accept.json {
          render :json => {:success => false, :message => "Your account could not be created. Server error:\n#{error_text}"}
        }
      ## TODO add render :xml => {blah-blah-blah}
      end
    end
  end

  def update
## test code, check the privilege system
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
        render :json => "hi"
      }
    end
  end

private

  def root_or_admin_required
    redirect_to login_url unless logged_in? and (current_user.in_group? "admin" or current_user.in_group? "root")
  end

end
