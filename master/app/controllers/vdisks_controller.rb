class VdisksController < ApplicationController

  before_filter :login_required

  def index
    result = []
    Vdisk.all.each do |vdisk|
      result << {
        :id => vdisk.id,
        :raw_name => vdisk.raw_name,
        :display_name => vdisk.display_name
      }
    end
    render_data result
  end

end
