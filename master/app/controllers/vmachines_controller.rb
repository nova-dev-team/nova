# This is the controller for VMachines.
#
# Author::  Santa Zhang (mailto:santa1987@gmail.com)
# Since::   0.3
  
class VmachinesController < ApplicationController

  before_filter :login_required

  # Change settings of the VM.
  #
  # Since::   0.3
  def edit
    vm = load_vm
    return if vm == nil
    if valid_param? params[:item]
      case params[:item]
      when "name"
        vm.name = vm.vcluster.cluster_name + "-" + params[:value]
      when "cpu_count"
        vm.cpu_count = params[:value]
      when "mem_size"
        vm.memory_size = params[:value]
      else
        reply_failure "Cannot understand item '#{params[:item]}'!"
        return false
      end
      if vm.save
        reply_success "VM settings updated."
      else
        reply_failure "Failed to edit VM settings!"
      end
    else
      reply_failure "Please provide 'item' and 'value' parameters!"
    end
  end

  # Change a VM's status from 'shut-off' to 'start-pending'.
  #
  # Since::   0.3
  def start
    vm = load_vm
    return if vm == nil
    if vm.status != "shut-off"
      reply_failure "The VM with UUID='#{params[:uuid]}' is not in 'shut-off' status!"
      return false
    else
      vm.status = "start-pending"
      vm.save
      reply_success "The VM with UUID='#{params[:uuid]}' is now pending start."
    end
  end

  # Force the VM to be in 'shut-off' status.
  # Could be used in those status: start-pending, start-preparing, running, suspended, connect-failure.
  #
  # Since::   0.3
  def shut_off
    vm = load_vm
    return if vm == nil
    case vm.status
    when "start-pending"
      vm.status = "shut-off"
      vm.save
      reply_success "The VM with UUID='#{params[:uuid]}' is now shut off."
    when "start-preparing", "running", "suspended", "connect-failure"
      vm.status = "shutdown-pending"
      vm.save
      reply_success "The VM with UUID='#{params[:uuid]}' is now pending shut off."
    else
      reply_failure "Cannot do this on VM with status='#{vm.status}'!"
    end
  end

  # remove the error messages for the VM.
  #
  # Since::   0.3
  def reset_error
    vm = load_vm
    return if vm == nil
    case vm.status
    when "boot-failure", "connect-failure"
      vm.status = "shut-off"
      vm.save
      reply_success "The error status of VM with UUID='#{params[:uuid]}' has been cleaned."
    else
      reply_failure "Could only do this on VMs in failure status!"
    end
  end

private

  # Check if current user has enough privilege to manipulate the VM.
  #
  # Since::   0.3
  def check_privilege vm
    # if user is not root, check privileges
    if @current_user.privilege != "root"
      unless vm.user == @current_user
        reply_failure "You are not allowed to do this!"
        return false
      end
    end
    return true
  end

  # Get the VM from user's request.
  #
  # Since::   0.3
  def load_vm
    if valid_param? params[:uuid]
      vm = Vmachine.find_by_uuid params[:uuid]
      if vm == nil
        reply_failure "Cannot find VM with UUID='#{params[:uuid]}'!"
      end
      return vm
    else
      reply_failure "Please provide the 'uuid' parameter!"
      return nil
    end
  end

end
