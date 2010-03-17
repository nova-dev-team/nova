# This is a helper to all components.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

require "#{RAILS_ROOT}/../common/lib/controller_utility.rb"
require 'yaml'

# Reopens the 'ControllerUtility', modify it for master.
module ControllerUtility

protected

  # Check if a component is enabled.
  #
  # Since::     0.3
  def component_enabled? component_name
    enabled_components = YAML::load File.read "#{RAILS_ROOT}/config/enabled_components.yml"
    enabled_components[component_name]
  end

  # Check if the system is fully installed.
  #
  # Since::     0.3
  def check_if_installed?
    if component_enabled? "installer"
      redirect_to :controller => :install, :action => :index
    end
  end

end
