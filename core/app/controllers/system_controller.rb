# this controller is used to observer system status
# it could only be triggered by "root"/"admin" group

class SystemController < ApplicationController

  # show system information
  def info
    if current_user and current_user.groups.find_by_name "root"
      render :text => "HI"
    else # not root user
      render :text => "YOUR ARE NOT ALLOWED TO USE THIS"
    end
  end
  
end
