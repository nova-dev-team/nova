# Model for physical machines.
#
# Author::  Santa Zhang (mailto:santa1987@gmail.com)
# Sinc::    0.3

class Pmachine < ActiveRecord::Base

  has_many :vmachines

  # Try to connect to pmachine. The pmachine's status will be changed to "pending",
  # and we try to connect to it by RestClient. If connection is successful, the status
  # will be changed to "working", otherwise it will be marked as ""
  #
  # * This function should be called by daemons, since it might take a long time to response!
  #
  # Since::     0.3
  def connect
  end

  # Mark a pmachines are "retired", it will not serve VMs any more. The VMs being used on it will
  # be kept, while idle VMs will be destroyed.
  #
  # Since::     0.3
  def retire
  end

  # Unmark a pmachine from "retired" status, change it to "pending" status
  def reuse
  end

end
