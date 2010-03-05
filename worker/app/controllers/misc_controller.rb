class MiscController < ApplicationController

  def hi
    render_success "Hi"
  end

  def role
    render_success "worker"
  end

  def whoami
    render_success "Worker"
  end

end
