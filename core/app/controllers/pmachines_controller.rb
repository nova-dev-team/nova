class PmachinesController < ApplicationController
  include Authentication
  include Authentication::ByPassword
  include Authentication::ByCookieToken
  include UsersHelper
  include AuthenticatedSystem

  def new
    UsersHelper::if_authorized? (:new, :pmachine) do
      render :text => "HI"
    end
  end

end
