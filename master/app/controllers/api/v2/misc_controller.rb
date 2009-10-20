## class that handles miscellaneous actions
class Api::V2::MiscController < Api::V2::ApplicationController

  def hi
    render_success "HI"
  end

  def whoami
    render_success "Master"
  end

end
