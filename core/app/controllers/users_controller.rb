require 'pp'

class UsersController < ApplicationController

  # render new.rhtml
  def new
    @user = User.new
  end
 
  def create
    logout_keeping_session!
    @user = User.new(params[:user])
    ## TODO add into admin group?
    @user.groups << (Group.find_by_name 'user') ## default user group
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


## Update a user's settings, such as fullname, password, email address.
## Also used to change activation and groups, which requires privileged user.
## A user cannot change his/her own activation flag and groups
## When changing password, old password is also required, along with a password confirmation.
## When changing full username and email address, they must be well formatted as required by "User" model.
  def update
    pp params
    
    information_updated = false # flag to denote a change in user's profile
    
    if params[:fullname]
      assert_self params[:id] # only user him/herself could change these properties
      # TODO
      information_updated = true
    end
    
    if params[:email]
      assert_self params[:id] # only user him/herself could change these properties
      # TODO
      information_updated = true
    end
    
    if params[:new_password] || params[:new_password_confirm] || params[:old_password]
      assert_self params[:id] # only user him/herself could change these properties
      # TODO
      information_updated = true
    end

    if params[:activated]
    
      root_or_admin_required
      u = User.find_by_id params[:id]
      u.activated = (params[:activated] == 'true')
      if u.save
        information_updated = true
      else
        ## TODO alert saving error
      end
    end
    
    reply_message = ""
    reply_success = false
    if information_updated
      reply_message = "Successfully changed settings of '#{u.login}'"
      reply_success = true
    else
      reply_message = "Nothing updated for '#{u.login}'"
      reply_success = false
    end
    
    respond_to do |accept|
      accept.json {
        render :json => {:success => reply_success, :message => reply_message}
      }
    end
    
  end

private

  def deny_request
    render :status => :forbidden
  end

  def assert_self user_id
    deny_request unless current_user.login == (User.find user_id).login
  end

  def root_or_admin_required
    redirect_to login_url unless logged_in? and (current_user.in_group? "admin" or current_user.in_group? "root")
  end

end

