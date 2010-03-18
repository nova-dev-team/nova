# A helper controller that does many things.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

class MiscController < ApplicationController

  # Reply the role of this module.
  #
  # Since::     0.3
  def role
    reply_success "vm_pool"
  end

  # Acquire a VM from the pool.
  #
  # Since::     0.3
  def acquire
    reply_failure "TODO"
  end

  # Release an acquired VM back into the pool.
  #
  # Since::     0.3
  def release
    reply_failure "TODO"
  end

end
