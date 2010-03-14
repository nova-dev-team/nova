# A special component of Nova, works like a vm_pool.
# All workers hosts several running vmachines, and when user requested for a new
# machine, some VM is allocated to the user.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

class VmPoolController < ApplicationController
  
  before_filter :check_if_enabled

  def index
    reply_success "TODO"
  end

private

  def check_if_enabled
    unless component_enabled? "vm_pool"
      reply_failure "Sorry, the 'vm_pool' component is not enabled!"
    end
  end

end
