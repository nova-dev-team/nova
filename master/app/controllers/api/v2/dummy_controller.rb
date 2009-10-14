## class that handles miscellaneous actions
class Api::V2::DummyController < Api::V2::ApplicationController

  def hi
    render_success "hi"
  end

end
