## class that handles miscellaneous actions
class Api::V3::MiscController < Api::V3::ApplicationController

  def version
    render_success "Version: 3.0"
  end

end
