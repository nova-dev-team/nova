# this controller is used to set system settings
# it could only be triggered by "root" group

class SystemController < ApplicationController

  # change settings
  def set
    if current_user and current_user.groups.find_by_name "root"
      render :text => "HI"
    else # not root user
      render :text => "YOUR ARE NOT ALLOWED TO USE THIS"
    end
  end

  # show system information
  def info
  end
  
end
