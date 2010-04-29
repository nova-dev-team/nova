# Controller for plain web ui.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class WebuiController < ApplicationController

  before_filter :login_required
  
  # The main entry page. Detail is implemented in "view/webui" folder.
  #
  # Since::   0.3
  def index
  end

end

