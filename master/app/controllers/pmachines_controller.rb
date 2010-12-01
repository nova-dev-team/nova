# The controller for physical machines.
# It is adapted from "vm_pool" component.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require "utils.rb"
require "timeout"

class PmachinesController < ApplicationController

  before_filter :root_required

  # List all the running VMs
  #
  # Since::     0.3
  def list
    list_data = []
    Pmachine.all.each do |pm|
      pm_data = {
        :id => pm.id,
        :ip => pm.ip,
        :hostname => pm.hostname,
        :mac_addr => pm.mac_address,
        :status => pm.status,
        :vm_capacity => pm.vm_capacity
      }
      vm_preparing = 0
      vm_failure = 0
      vm_running = 0
      pm.vmachines.each do |vm|
        if vm.status.downcase == "preparing"
          vm_preparing += 1
        elsif vm.status.downcase == "failure"
          vm_failure += 1
        elsif vm.status.downcase == "running"
          vm_running += 1
        end
      end
      pm_data[:vm_preparing] = vm_preparing
      pm_data[:vm_failure] = vm_failure
      pm_data[:vm_running] = vm_running
      list_data << pm_data
    end
    reply_success "query successful!", :data => list_data
  end

  # Shut down all vm on a pmachine
  #
  # Since::     0.3.6
  def power_off_all_vm
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]

    if pm == nil
      reply_failure "No pmachine with ip=#{params[:ip]} was found!"
    elsif pm.status != "working"
      reply_failure "Pmachine with ip=#{params[:ip]} is not working!"
    else
      pm.vmachines.each do |vm|
        case vm.status.downcase
        when "start-preparing", "suspended", "running"
          vm.status = "shutdown-pending"
          vm.save
        end
      end
      reply_success "All VM on ip=#{params[:ip]} was marked as shutdown-pending."
    end
  end

  # Resume all vm on a pmachine
  #
  # Since::     0.3.6
  def resume_all_vm
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]

    if pm == nil
      reply_failure "No pmachine with ip=#{params[:ip]} was found!"
    elsif pm.status != "working"
      reply_failure "Pmachine with ip=#{params[:ip]} is not working!"
    else
      begin
        rep = pm.worker_proxy.resume_all
        if rep["success"].to_s == "true"
          reply_success "All VM on ip=#{params[:ip]} was being resumed."
        else
          reply_failure "Operation failed: #{rep["message"]}"
        end
      rescue Exception => e
        reply_failure "Exception: #{e.to_s}"
      end
    end
  end

  # Suspend all vm on a pmachine
  #
  # Since::     0.3.6
  def suspend_all_vm
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]

    if pm == nil
      reply_failure "No pmachine with ip=#{params[:ip]} was found!"
    elsif pm.status != "working"
      reply_failure "Pmachine with ip=#{params[:ip]} is not working!"
    else
      begin
        rep = pm.worker_proxy.suspend_all
        if rep["success"].to_s == "true"
          reply_success "All VM on ip=#{params[:ip]} was being suspended."
        else
          reply_failure "Operation failed: #{rep["message"]}"
        end
      rescue Exception => e
        reply_failure "Exception: #{e.to_s}"
      end
    end
  end

  # Show all info of every pmachine
  #
  # Since::     0.3.6
  def show_all_info
    all_data = []
    Pmachine.all.each do |pm|
      pm_data = {
        :id => pm.id,
        :ip => pm.ip,
        :hostname => pm.hostname,
        :status => pm.status,
        :vm_capacity => pm.vm_capacity,
        :vm_list => []
      }
      pm.vmachines.each do |vm|
        pm_data[:vm_list] << {:name => vm.name, :status => vm.status}
      end
      all_data << pm_data
    end
    reply_success "Query successful!", :data => all_data
  end

  # Show detail info of a pmachine
  # Param:
  #   ip: The ip address of pmachine.
  #
  # Since::     0.3.3
  def show_info
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]

    if pm != nil
      pm_data = {
        :id => pm.id,
        :ip => pm.ip,
        :hostname => pm.hostname,
        :status => pm.status,
        :vm_capacity => pm.vm_capacity,
        :vm_list => []
      }
      pm.vmachines.each do |vm|
        pm_data[:vm_list] << vm
      end
      reply_success "Query successful!", :data => pm_data
    else
      reply_failure "Pmachine with ip=#{params[ip]} not found!"
    end
  end

  # Power off a pmachine.
  # Params:
  #   ip: The ip address of the machine
  #
  # Since::     0.3.5
  def power_off
    if params[:ip]
      fork do
        timeout(10) do
          `ssh #{params[:ip]} "shutdown -h 0"`
        end
        exit
      end
      reply_success "The pmachine will shutdown soon."
    else
      reply_failure "Please check your params!"
    end
  end

  # Power on a pmachine
  # Params:
  #   ip: The ip address of the pmachine.
  #   mac_addr: (optional) The mac address of the pmachine.
  #
  # Since::     0.3.5
  def power_on
    mac_addr = nil
    if params[:mac_addr]
      mac_addr = params[:mac_addr]
    elsif params[:ip]
      pm = Pmachine.find_by_ip params[:ip]
      mac_addr = pm.mac_address
    end
    if mac_addr != nil
      `ether-wake #{mac_addr}`
      reply_success "The pmachine will start soon."
    else
      reply_failure "Please check your params!"
    end
  end

  # Add a new pmachine entry.
  # Params:
  #   ip: the ip address of the new pmachine
  #   vm_capacity: a suggested limit on number of vmachines
  #
  # Since::     0.3
  def add
    return unless valid_capacity? and valid_ip?

    pm = Pmachine.find_by_ip params[:ip]
    if pm != nil
      reply_failure "Pmachine with IP=#{params[:ip]} already added!"
    else
      pm = Pmachine.new
      pm.ip = params[:ip]
      pm.vm_capacity = params[:vm_capacity].to_i
      pm.status = "pending"
      pm.save
      reply_success "Pmachine added. It is now in 'pending' status, and will be connected very soon."
    end
  end

  # Mark the pmachine as "to be reconnected". The connection job is left for background processes.
  #
  # Since::     0.3
  def reconnect
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    else
      pm.status = "pending"
      pm.save
      reply_success "Pmachine with IP=#{params[:ip]} changed to 'pending' status."
    end
  end

  # Mark pmachines with "retired" tag as "to be reconnected".
  #
  # Since::     0.3
  def reuse
    reconnect # reuse is basically "reconnect"
  end

  # Mark a machine as "retired".
  #
  # Since::     0.3
  def retire
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    else
      pm.status = "retired"
      pm.save
      reply_success "Pmachine with IP=#{params[:ip]} changed to 'retired' status."
    end
  end

  # Change the "vm_capacity" of this pmachine.
  #
  # Since::     0.3
  def edit_capacity
    return unless valid_capacity? and valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    else
      pm.vm_capacity = params[:vm_capacity].to_i
      pm.save
      reply_success "Pmachine with IP=#{params[:ip]} has changed 'vm_capacity' to #{pm.vm_capacity}."
    end
  end

  # Delete the pmachine, use with care!
  # It is only possible when these conditions met:
  #
  #   * no VM running on the physical machine.
  #   * and the machine is retired or in 'failure' status.
  #
  # Since::     0.3
  def delete
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    elsif pm.vmachines.length != 0
      reply_failure "Cannot delte the pmachine, since there is still #{pm.vmachines.length} VM running on it!"
    elsif pm.status != "retired" and pm.status != "failure"
      reply_failure "The pmachine could only be deleted when it is 'retired' or in 'failure' status!"
    else
      Pmachine.delete pm
      reply_success "Pmachine with IP=#{params[:ip]} deleted."
    end
  end

private

  # Check if provided valid ip address.
  #
  # Since::     0.3
  def valid_ip?
    unless valid_param? params[:ip] and params[:ip].is_ip_addr?
      reply_failure "Invalid ip address!"
      return false
    end
    return true
  end

  # Check if provided valid 'vm_capacity'.
  #
  # Since::     0.3
  def valid_capacity?
    unless valid_param? params[:vm_capacity] and params[:vm_capacity].to_i.to_s == params[:vm_capacity]
      reply_failure "Invalid 'vm_capacity'!"
      return false
    end
    return true
  end

end
