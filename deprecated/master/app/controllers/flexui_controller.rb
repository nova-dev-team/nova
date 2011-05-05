# The controller for rendering Flex UI.
#
# Author::    Santa Zhang
# Since::     0.3

class FlexuiController < ApplicationController

  before_filter :login_required

  # This is a tricky action handler, which redirects to a full url.
  # This prevents the ambiguity of "/flex" and "/flex/index", and thus avoids the problem in relative path.
  #
  # Since::   0.3
  def index
    if File.exists? "#{RAILS_ROOT}/public/flexui/NovaFlexUI.html"
      redirect_to :controller => :flexui, :action => :ui
    else
      redirect_to :controller => :webui, :action => :index
    end
  end

  # The handler which renders the Flex container.
  #
  # Since::   0.3
  def ui
    render :file => "#{RAILS_ROOT}/public/flexui/NovaFlexUI.html"
  end

end
