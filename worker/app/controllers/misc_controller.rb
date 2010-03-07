# This controller is used to do miscellanous works. Generally speaking, it supports other controllers.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3


class MiscController < ApplicationController

  # Reply the role of this node. When adding workers, master uses this service to "authenticate" workers.
  #
  # Since::     0.3
  def role
    render_success "worker"
  end

end
