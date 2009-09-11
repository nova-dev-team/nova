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
      size = params[:size].to_i
      soft_list = "common\nssh-nopass\n" + params[:soft_list]
      vc = Vcluster.alloc(params[:name], soft_list, size)

      if vc == nil
        render_failure "Not enough resource!"
        return
      end

      # TODO advanced settings, each machines could have different hardware setting
      vc.vmachines.each do |vm|
        vm.cpu_count = params[:cpu_count]
        vm.memory_size = params[:memeory_size]
        vm.hda = params[:hda]
        vm.hdb = params[:hdb]
        vm.cdrom = params[:cdrom]
        vm.boot_device = params[:boot_device]
        vm.architecture = params[:architecture]
        vm.save
      end

      vc.user = current_user # mark vcluster ownership
      vc.save
      render_data vc
    else
      render_failure "Please provide 'size', 'name', 'soft_list' parameters"
    end
  end

end
