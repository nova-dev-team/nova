class AppController < ApplicationController
  
  before_filter :login_required
  
## TODO check user privilege for all actions

  def users
    if (current_user.in_group? "root") or (current_user.in_group? "admin")
      render :template => 'app/users.html.erb', :layout => 'default'
    else
      render_error "You do not have enough privilege for this action"
    end
  end
  
  
  def admin_machines
    if current_user.in_group? "admin"
      render :template => 'app/admin_machines.html.erb', :layout => 'default'
    else
      render_error "You do not have enough privilege for this action"
    end
  end
  
  def admin_resources
    if current_user.in_group? "admin"
      render :template => 'app/admin_resources.html.erb', :layout => 'default'
    else
      render_error "You do not have enough privilege for this action"
    end
  end
  
  def root_machines
    if current_user.in_group? "root"
      render :template => 'app/root_machines.html.erb', :layout => 'default'
    else
      render_error "You do not have enough privilege for this action"
    end
  end
  
  def root_resources
    if current_user.in_group? "root"
      render :template => 'app/root_resources.html.erb', :layout => 'default'
    else
      render_error "You do not have enough privilege for this action"
    end
  end
  
  def root_system_settings
    if current_user.in_group? "root"
      render :template => 'app/root_system_settings.html.erb', :layout => 'default'
    else
      render_error "You do not have enough privilege for this action"
    end
  end

  def home
    if current_user.in_group? "root"
      render :template => 'app/root_home.html.erb', :layout => 'default'
    elsif current_user.in_group? "admin"
      render :template => 'app/admin_home.html.erb', :layout => 'default'
    else
      render :template => 'app/user_home.html.erb', :layout => 'default'
    end
  end
  
  

private

  def render_error error_msg
    render :text => error_msg
  end

  def login_required
    redirect_to login_url unless logged_in?
  end

end

