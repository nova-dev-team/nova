# Controller for the user accounts.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class UsersController < ApplicationController

  # Create new user account. This is just an API, with no HTML web pages.
  #
  # Author::    Santa Zhang
  # Since::     0.3
  def create
    logout_keeping_session!
    @user = User.new(params)
    if @user
      if params[:privilege] != "admin"
        @user.privilege = "normal_user"
      else
        @user.privilege = "admin"
      end
    end
    success = @user && @user.save
    if success && @user.errors.empty?
      # Protects against session fixation attacks, causes request forgery
      # protection if visitor resubmits an earlier form using back
      # button. Uncomment if you understand the tradeoffs.
      # reset session

      # Santa Zhang:
      #
      # don't sign in the user immediately, since we need to activate it by admin
      # self.current_user = @user # !! now logged in
      reply_success "Your account has been registered. Please wait for administrator's approval."
    else
      error_text = @user.errors.collect {|item, reason| "#{item}: #{reason}\n"}
      reply_failure "Your account could not be created. Server error:\n#{error_text}"
    end
  end


  # Show a list of users. Login required.
  # Only the user himself and users with lower privilege will be listed.
  # Paging is supported. You can use 'page' to denote the page id (starts from 1), and 'page_size' to denote number of elements on one page.
  #
  # Since::     0.3
  def list

    # login required
    unless logged_in?
      reply_failure "You are not logged in!"
      return
    end

    if params[:page]
      unless params[:page_size]
        reply_failure "If you want to enabled paging, add the 'page_size' parameter which indicates number of users to be shown in one page!"
        return
      end
      unless params[:page].to_s == params[:page].to_i.to_s and params[:page_size].to_s == params[:page_size].to_i.to_s and params[:page].to_i > 0 and params[:page_size].to_i > 0
        reply_failure "Please provide valid 'page' and 'page_size' parameter!"
        return
      end
    end

    if @current_user.privilege == "root"
      users_list = User.all
    elsif @current_user.privilege == "admin"
      users_list = User.find(:all, :conditions => ["privilege = 'normal_user' or login = ?", @current_user.login])
    else
      users_list = [@current_user]
    end

    if params[:page]
      page_size = params[:page_size].to_i
      page_begin = (params[:page].to_i - 1) * page_size
      page_end = page_begin + page_size
      if page_begin >= users_list.size
        reply_failure "Page number is too big!"
        return
      end
      if page_end >= users_list.size
        page_end = users_list.size
      end
      pages_total = (users_list.size + page_size - 1) / page_size

      # note that we used "..." here, last item (page_end) is not included
      users_list = users_list[page_begin...page_end]
    end

    info_list = users_list.collect do |user|
      { :id => user.id, :login => user.login, :name => user.name, :email => user.email, :privilege => user.privilege, :activated => user.activated }
    end

    if defined? pages_total
      reply_success "Successfully retrieved list of users", :users => info_list, :pages_total => pages_total
    else
      reply_success "Successfully retrieved list of users", :users => info_list
    end

  end


  # Update a user's information, such as full name, email address, and password. Login required.
  # It is also used to activate/deactivate users.
  # Note that user's full name and email address should be well formatted, since they will be checked when saving new infomation.
  #
  # For "normal user", nothing except his own information could be retireved/changed.
  # Root & admin could activate/deactivate users with lower privilege.
  #
  # When changing password, old password and new password confirmation is also required.
  #
  # Since::     0.3
  def edit
    # login required
    unless logged_in?
      reply_failure "You are not logged in!"
      return
    end

    if params[:login] == nil or params[:login] == ""
      reply_failure "You must provide the user's login name!"
      return
    end

    # prevent normal user from retrieving any information except his own
    if @current_user.privilege != "root" and @current_user.privilege != "admin"
      if @current_user.login != params[:login]
        reply_failure "You are not allowed to change anything except your own information!"
        return
      end
    end

    # This is a flag to indicate whether the user's profile is actually changed.
    information_updated = false

    user = User.find_by_login params[:login]
    if user == nil
      reply_failure "User with login='#{params[:login]}' not found!"
      return
    end

    if params[:name]
      # only the user him/herself could change these properties
      if @current_user.id == user.id
        if user.name != params[:name]
          information_updated = true
          user.name = params[:name]
        end
      else
        reply_failure "You cannot change other user's private information!"
        return
      end
    end

    if params[:email]
      if @current_user.id == user.id
        # only the user him/herself could change these properties
        if user.email != params[:email]
          information_updated = true
          user.email = params[:email]
        end
      else
        reply_failure "You cannot change other user's private information!"
        return
      end
    end

    if params[:new_password] || params[:new_password_confirm] || params[:old_password]
      if @current_user.id != user.id
        reply_failure "You cannot change other user's password!"
        return
      end

      if !(params[:new_password] and params[:new_password_confirm] and params[:old_password])
        reply_failure "Please provide old password, new password and new password confirmation!"
        return
      end

      if User.authenticate @current_user.login, params[:old_password]
        user.password = params[:new_password]
        user.password_confirmation = params[:new_password_confirm]
        information_updated = true
      else
        reply_failure "Wrong old password!"
        return
      end
    end

    # Activate users. Only root and admin could do this!
    if params[:activated]
      if @current_user.privilege == "root"
        # root could activate/deactivate anyone except itself
        if user.login == @current_user.login
          reply_failure "You cannot deactivate yourself!"
          return
        end
      elsif @current_user.privilege == "admin"
        if user.login == @current_user.login
          reply_failure "You cannot deactivate yourself!"
          return
        elsif user.privilege == "root"
          reply_failure "You cannot deactivate root user!"
          return
        elsif user.privilege == "admin"
          reply_failure "You cannot deactivate admin users!"
          return
        end
      else
        # normal users, disallow
        reply_failure "You are not allowed to do this!"
        return
      end

      if user.activated.to_s != params[:activated].to_s
        user.activated = (params[:activated] == 'true')
        information_updated = true
      end
    end

    if information_updated and user.save
      reply_success "Successfully changed properties of '#{user.login}'"
    else
      reply_success "Nothing updated for '#{user.login}'"
    end
  end

end

