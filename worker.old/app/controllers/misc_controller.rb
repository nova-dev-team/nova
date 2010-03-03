class MiscController < ApplicationController

  def hi
    render_success "Hi"
  end

  def whoami
    render_success "Worker"
  end

end
