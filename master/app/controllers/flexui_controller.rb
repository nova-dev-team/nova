# Controller for the Flex ui.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class FlexuiController < ApplicationController

  before_filter :login_required

  # Return the Flex container page.
  #
  # Since::   0.3
  def index
  end

end
