require 'pp'

class UsersController < ApplicationController

  # render new.rhtml
  def new
    @user = User.new
  end
 
  def create
    logout_keeping_session!
    @user = User.new(params)
    if params[:group] != "admin"
      @user.groups << (Group.find_by_name 'user') ## default user group
    else
      @user.groups << (Group.find_by_name 'admin')
    end
    success = @user && @user.save
    if success && @user.errors.empty?
            # Protects against session fixation attacks, causes request forgery
      # protection if visitor resubmits an earlier form using back
      # button. Uncomment if you understand the tradeoffs.
      # reset session

      ## don't sign in the user immediately, since we need to activate it by admin
      # self.current_user = @user # !! now logged in
      
      
      render_success "Your account has been registered. Please wait for administrator's approval."
      
      ## redirect_back_or_default('/')
      ## flash[:notice] = "Thanks for signing up!  We're sending you an email with your activation code."
    else
      ## flash[:error]  = "We couldn't set up that account, sorry.  Please try again, or contact an admin (link is above)."
      ## render :action => 'new'

      error_text = @user.errors.collect {|item, reason| "#{item}: #{reason}\n"}
      render_failure "Your account could not be created. Server error:\n#{error_text}"

    end
  end


## Show (partial) list of users
## For normal user, only him/herself was shown
## For root user, all users are shown, including "root"
## For admin user, all normal users and him/herself was shown
## TODO
  def index
  
    if not logged_in_and_activated?
      render_failure "You must be logged in to carry out this action"
      return
    end
    
    # helper funciton to select fields to respond
    def user_data_in_hash u
      {:id => u.id, :login => u.login, :fullname => u.name, :email => u.email, :groups => u.groups.collect {|g| g.name}, :activated => u.activated}
    end
    
    if current_user.in_group? "root"
      users_list = User.all.collect {|u| user_data_in_hash u}
    elsif current_user.in_group? "admin"
      users_list = (User.all.select {|u| u.in_group? "user"}).collect {|u| user_data_in_hash u}
      users_list << (user_data_in_hash current_user)
    else
      users_list = [user_data_in_hash current_user]
    end
    
    render_success "Successfully retrieved list of users", {:users => users_list}
    
  end

  def whoami
    if not logged_in_and_activated?
      render_failure "You must be logged in to carry out this action"
      return
    end

    render_success "Successfully retrieved your information", {
      :name => current_user.name,
      :email => current_user.email,
      :login_id => current_user.login,
      :groups => current_user.groups.collect {|g| g.name}
    }
  end

## Update a user's settings, such as fullname, password, email address.
## Also used to change activation and groups, which requires privileged user.
## A user cannot change his/her own activation flag and groups
## When changing password, old password is also required, along with a password confirmation.
## When changing full username and email address, they must be well formatted as required by "User" model.
  def edit
  
    # require logged in
    if not logged_in_and_activated?
      render_failure "You are not logged in"
      return
    end
  
    information_updated = false # flag to denote a change in user's profile
    
    user = User.find_by_id params[:id]
    if user == nil
      render_failure "User not found"
      return
    end
    
    if params[:fullname]
      # only user him/herself could change these properties
      if logged_in_and_activated? and current_user.id == user.id
        information_updated = true
        user.name = params[:fullname]
        # length test is take when saving to database
      else
        render_failure "You are not allowd to do this"
        return
      end
    end
    
    if params[:email]
      if logged_in_and_activated? and current_user.id == user.id
        information_updated = true
        user.email = params[:email]
      else
        render_failure "You are not allowd to do this"
        return
      end
    end
    
    if params[:new_password] || params[:new_password_confirm] || params[:old_password]
      if !logged_in_and_activated? or current_user.id != user.id
        render_failure "You are not allowd to do this"
        return
      end
      
      if !(params[:new_password] and params[:new_password_confirm] and params[:old_password])
        render_failure "Please provide old password, new password and new password confirmation"
        return
      end

      if User.authenticate current_user.login, params[:old_password]
        user.password = params[:new_password]
        user.password_confirmation = params[:new_password_confirm]
        information_updated = true
      else
        render_failure "Wrong old password"
        return
      end
    end

    if params[:activated] # root or admin required
      if current_user.is_root? # root could active anyone except itself
      
        if user.is_root? # trying to change settings of root itself, disallowed
          render_failure "Root user cannot be deactivated!"
          return
        end
        
      elsif current_user.is_admin?

        if user.is_admin? # trying to change settings of admins, disallowed
          render_failure "Administrators cannot be deactivated!"
          return
        elsif user.is_root? # cannot deactivate root
          render_failure "You are not allowed to do this!"
          return
        end
        
      else # normal user cannot do this
        render_failure "You do not have enough priviledge for this action!"
        return
      end
      
      user.activated = (params[:activated] == 'true')
      information_updated = true
    end
    
    if information_updated and user.save
      render_success "Successfully changed settings of '#{user.login}'"
    else
      render_failure "Nothing updated for '#{user.login}'"
    end
#  rescue
 #   render_failure "Exception occured on server"
  end

end

