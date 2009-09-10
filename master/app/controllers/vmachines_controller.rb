class VmachinesController < ApplicationController

  before_filter :login_required

  def index
    result = []
    Vmachine.all.each do |vmachine|
      result << "vm#{vmachine.id}:#{vmachine.uuid}"
    end

    render_data result
  end

end
