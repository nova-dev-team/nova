class HotbackupController < ApplicationController

  before_filter :root_required

  def index
   reply_model Hotbackup
  end

  def suggest
    backuped_vms = Hotbackup.all.collect {|hb| hb.vmachine.uuid}
    available_vms = []
    available_pms = []
    Pmachine.all.each do |pm|
      next if pm.status != "working"
      available_pms << pm.ip
    end

    Vmachine.all.each do |vm|
      next if backuped_vms.include? vm
      next if vm.status != "running"
      vm_uuid = vm.uuid
      vm_pm_ip = vm.pmachine.ip
    end
    reply_success "Query successful!", :vmachines => available_vms, :pmachines => available_pms
  end

  def add
    vm = Vmachine.find_by_uuid params[:uuid]
    slave_pm = Pmachine.find_by_ip params[:slave_ip]
    if vm == nil or slave_pm == nil
      reply_failure "Please check your params (uuid, slave_ip)!"
    elsif slave_pm == vm.pmachine
      reply_failure "Cannot hot backup to self!"
    else
      hb = Hotbackup.new
      hb.vmachine = vm
      hb.from_ip = vm.pmachine.ip
      hb.to_ip = params[:slave_ip]
      hb.save
      reply_success "Done!"
    end
  end
  
  def remove
    hb = Hotbackup.find_by_id params[:id]
    Hotbackup.delete hb
    reply_success "Done!"
  end

end
