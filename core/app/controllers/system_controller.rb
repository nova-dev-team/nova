# this controller is used to observer system status
# it could only be triggered by "root"/"admin" group

class SystemController < ApplicationController

  before_filter :root_required

  # show system information
  def index
    render :text => "YOU are in root group, right?"
  end

private

  def root_required
    redirect_to login_url unless logged_in? and current_user.in_group? "root"
  end
  
end
