class VmachinesController < ApplicationController

  before_filter :login_required

  def index
    result = []
    Vmachine.all.each do |vmachine|
      result << "vm#{vmachine.id}:#{vmachine.uuid}"
    end

    render_data result
  end

  def show
    vm = Vmachine.find params[:id]
    result = {
      :id => vm.id,
      :mem_size => vm.memory_size,
      :cpu_count => vm.cpu_count,
      :uuid => vm.uuid,
      :hda => vm.hda,
      :hdb => vm.hdb,
      :cdrom => vm.cdrom,
      :boot_device => vm.boot_device,
      :arch => vm.arch,
      :pmachine_addr => (vm.pmachine ? vm.pmachine.addr : nil),
      :vcluster_id => vm.vcluster.id,
      :vcluster_name => vm.vcluster.cluster_name,
      :soft_list => vm.vcluster.package_list,
      :status => vm.status,
      :vnc_port => 5900
    }
    render_data result
  end

  # vnc view
  def observe
    respond_to do |accept|
      accept.html {render :template => 'vmachines/observe'}
    end
  end

end
