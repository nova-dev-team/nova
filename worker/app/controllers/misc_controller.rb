# This controller is used to do miscellanous works. Generally speaking, it supports other controllers.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)


class MiscController < ApplicationController

  def hi
    render_success "Hi"
  end

  # Reply the role of this node. When adding workers, master uses this service to "authenticate" workers.
  #
  # Since::     0.3
  def role
    render_success "worker"
  end

  def whoami
    render_success "Worker"
  end

end
