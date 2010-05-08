# The controller for physical machines.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require "utils.rb"

class PmachinesController < ApplicationController

  # Add a new pmachine entry.
  #
  # Since::     0.3
  def add
    return unless valid_pool_size? and valid_ip?

    pm = Pmachine.find_by_ip params[:ip]
    if pm != nil
      reply_failure "Pmachine with IP=#{params[:ip]} already added!"
    else
      pm = Pmachine.new
      pm.ip = params[:ip]
      pm.vm_pool_size = params[:pool_size].to_i
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

  # Change the pool size of this pmachine.
  #
  # Since::     0.3
  def edit_pool_size
    return unless valid_pool_size? and valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    else
      pm.vm_pool_size = params[:pool_size].to_i
      pm.save
      reply_success "Pmachine with IP=#{params[:ip]} has changed pool size to #{pm.vm_pool_size}."
    end
  end

  # Delete the pmachine, use with care!
  # It is only possible when those conditions met:
  #
  #  * no VM running on the machine.
  #  * the machine is "retired" or in "failure" status.
  #
  # Since::     0.3
  def delete
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    elsif pm.vmachines.length != 0
      reply_failure "Cannot delete the pmachine, since there is still #{pm.vmachines.length} VM running on it."
    elsif pm.status != "retired" and pm.status != "failure"
      reply_failure "The pmachine could be deleted only if it is in 'retired' or 'failure' status!"
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

  # Check if provided valid pool size.
  #
  # Since::     0.3
  def valid_pool_size?
    unless valid_param? params[:pool_size] and params[:pool_size].to_i.to_s == params[:pool_size]
      reply_failure "Invalid pool size!"
      return false
    end
    return true
  end

end
