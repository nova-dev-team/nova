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
        render_failure "Not enough net pool left!"
        return
      end

      # TODO advanced settings, each machines could have different hardware setting
      vc.vmachines.each do |vm|
        vm.cpu_count = params[:cpu_count].to_i || vm.cpu_count
        vm.cpu_count = 1 if vm.cpu_count == 0

        vm.memory_size = params[:memory_size].to_i || vm.memory_size
        vm.hda = params[:hda] || "vd1-sys-empty10g.qcow2"
        vm.hdb = params[:hdb]
        vm.cdrom = params[:cdrom]
        vm.boot_device = params[:boot_device] || "hd"
        vm.arch = params[:arch]
        vm.save
      end

      if params[:start_now]
        pm_messages = []
        vc.vmachines.each do |vm|
          pm_messages << vm.start
        end
        # TODO render additional data, such as success rate..etc
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
