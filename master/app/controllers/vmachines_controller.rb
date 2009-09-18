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
    vm = Vmachine.find_by_id params[:id]
    if vm == nil
      vm = Vmachine.find_by_uuid params[:id] # try finding by uuid
    end
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
      :mac_addr => vm.mac,
      :vnc_port => vm.vnc_port
    }
    render_data result
  end

  # vnc view
  def observe
    respond_to do |accept|
      accept.html {render :template => 'vmachines/observe'}
    end
  end

  def start
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.start
    render_success "Successfully started vmachine with UUID #{params[:uuid]}."
  end

  def stop
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.stop
    render_success "Successfully stopped vmachine with UUID #{params[:uuid]}."
  end

  def resume
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.resume
    render_success "Successfully resumed vmachines with UUID #{params[:uuid]}"
  end

  def suspend
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.suspend
    render_success "Successfully suspended vmachine with UUID #{params[:uuid]}."
  end

end
