# The controller for displaying Nova's health status.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

class StatusController < ApplicationController

  # This is a tricky action handler, immediately redirects to a full url. This prevents broken relative address.
  #
  # Since::   0.3
  def index
    redirect_to :controller => :status, :action => :bulletin
  end

end

