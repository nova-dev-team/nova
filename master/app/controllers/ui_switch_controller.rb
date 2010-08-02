# This controller will dispatch user to plain UI or Flex UI.
# Flex UI is preferred.

class UiSwitchController < ApplicationController

  def index
    if File.exists? "#{RAILS_ROOT}/public/flexui/NovaFlexUI.html"
      redirect_to :controller => :flexui, :action => :ui
    else
      redirect_to :controller => :webui, :action => :index
    end
  end

end
