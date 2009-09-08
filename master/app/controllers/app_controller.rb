# this controller is intended for views


class AppController < ApplicationController
  
  before_filter :redirect_unless_logged_in

  def users
    if (current_user.in_group? "root") or (current_user.in_group? "admin")
      render :template => 'app/users.html.erb', :layout => 'default'
    else
      render_error_no_privilege
    end
  end
  
  
  def admin_machines
    render_only_for "admin"
  end
  
  def admin_resources
    render_only_for "admin"
  end
  
  def root_machines
    render_only_for "root"
  end
  
  def root_resources
    render_only_for "root"
  end
  
  def root_settings
    render_only_for "root"
  end
  
  def user_machines
    render_only_for "user"
  end
  
  def user_resources
    render_only_for "user"
  end

  def home
    if current_user.in_group? "root"
      render :template => 'app/root_home.html.erb', :layout => 'default'
    elsif current_user.in_group? "admin"
      render :template => 'app/admin_home.html.erb', :layout => 'default'
    elsif current_user.in_group? "user"
      render :template => 'app/user_home.html.erb', :layout => 'default'
    else
      render_error_no_privilege
    end
  end

private

  def redirect_unless_logged_in
    redirect_to login_url unless logged_in?
  end

end

