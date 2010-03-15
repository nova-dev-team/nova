# The installer component, used to aid system config & worker setup.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

class InstallController < ApplicationController

  before_filter :check_if_enabled

  def index
  end

private

  def check_if_enabled
    unless component_enabled? "installer"
      reply_failure "Sorry, the 'installer' component is not enabled!"
    end
  end
  
end
