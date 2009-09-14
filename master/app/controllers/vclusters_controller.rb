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

  def create
    if params[:name] and params[:size] and params[:soft_list]
      params[:soft_list] = "common\nssh-nopass\n" + params[:soft_list] unless params[:soft_list].start_with? "common\nssh-nopass\n"

      if params[:start_now]
        pp "[start_now]"
        vc = Vcluster.create_and_start params
      else
        vc = Vcluster.create params
      end

      vc.user = current_user # mark vcluster ownership
      vc.save
      render_data vc
    else
      render_failure "Please provide 'size', 'name', 'soft_list' parameters"
    end
  end

  def modify
  end

end
