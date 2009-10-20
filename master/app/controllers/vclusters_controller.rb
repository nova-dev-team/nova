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
      # format params list
      params[:soft_list] = params[:soft_list].split.join "\n"
      #params[:soft_list] = " + params[:soft_list] unless params[:soft_list].start_with? "common\nssh-nopass\n" # make sure common & ssh-nopass is selected
      

      
      puts "Soft to be installed:\n#{params[:soft_list]}\n"
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
    vm_info = vc.vmachines.collect do |vm|
      {
        :id => vm.id,
        :mem_size => vm.memory_size,
        :cpu_count => vm.cpu_count,
        :arch => vm.arch,
        :hda => vm.hda,
        :status => vm.status
      }
    end
    result = {
      :id => vc.id,
      :soft_list => vc.package_list,
      :name => vc.cluster_name,
      :size => vc.vmachines.size,
      :vm_info => vm_info
    }
    render_data result
  end

  def vm_list
    list = []
    vc = Vcluster.find_by_id params[:id]
    #vm = Vmachine.find_by_id params[:id]
    #if vm == nil
    #  vm = Vmachine.find_by_uuid params[:id] # try finding by uuid
    #end
    vc.vmachines.all.each do |vm|
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
      list << result
    end
    render_data list
  end

  def modify
  end

  # delete a whole cluster
  def destroy
    vc = Vcluster.find params[:id]
    vc.vmachines.each do |vm|
      vm.stop
    end
    render_success "Successfully destroyed vcluster named '#{vc.cluster_name}'"
    vc.destroy!
    Vcluster.delete vc
  end

end
