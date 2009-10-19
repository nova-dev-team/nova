## class that handles miscellaneous actions
class Api::V2::MiscController < Api::V2::ApplicationController

  def version
    render_success "Version: 2.0"
  end

end
