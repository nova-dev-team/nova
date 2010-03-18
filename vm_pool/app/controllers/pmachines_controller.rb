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

  def reconnect
    return unless valid_ip?
    pm = Pmachine.find_by_ip params[:ip]
    if pm == nil
      reply_failure "Pmachine with IP=#{params[:ip]} not found!"
    else
      pm.status = "pending"
      pm.save
      reply_success "Pmachine with IP=#{params[:ip]} changed to 'pending' statuus."
    end
  end

  def reuse
    reconnect # reuse is basically "reconnect"
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
