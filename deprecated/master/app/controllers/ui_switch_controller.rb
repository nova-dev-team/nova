# This controller will dispatch user to plain UI or Flex UI.
# Flex UI is preferred.

class UiSwitchController < ApplicationController

  # a switch before rendering index.html.
  # if flex ui exists, flex ui will be used; otherwise plain html ui will be used
  def index
    if File.exists? "#{RAILS_ROOT}/public/flexui/NovaFlexUI.html"
      redirect_to :controller => :flexui, :action => :ui
    else
      redirect_to :controller => :webui, :action => :index
    end
  end

end
