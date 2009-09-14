class VclustersController < ApplicationController

  before_filter :login_required

  def index
    result = []
    Vcluster.all.each do |vcluster|
      vm_list = vcluster.vmachines.collect do |vm|
        {
          :id => vm.id,
          :pm_addr => (vm.pmachine ? vm.pmachine.addr : nil)
        }
      end
      result << {
        :id => vcluster.id,
        :name => vcluster.cluster_name,
        :vm_list => vm_list
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

  def show
    vc = Vcluster.find params[:id]
    render_data vc
  end

  def modify
  end

end
