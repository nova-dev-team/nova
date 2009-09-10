class VclustersController < ApplicationController

  before_filter :login_required

  def index
    result = []
    Vcluster.all.each do |vcluster|
      result << {
        :id => vcluster.id,
        :name => vcluster.cluster_name
      }
    end

    render_data result
  end

end
