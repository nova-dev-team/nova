# The controller which manages the migration work.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

class MigrationController < ApplicationController

  before_filter :root_or_admin_required

  # List all the pmachines, and their vmachines.
  #
  # Since::   0.3
  def overview
    reply_data = []
    Pmachine.all.each do |pm|
      pm_data = {
        :ip => pm.ip, :hostname => pm.hostname, :vm_capacity => pm.vm_capacity
      }
      vm_all_data = []
      pm.vmachines.each do |vm|
        # TODO add info of vm migration, such as migrate_from, migrate_to, etc.
        vm_data = {
          :name => vm.name
        }
        vm_all_data << vm_data
      end
      pm_data[:vmachines] = vm_all_data
      reply_data << pm_data
    end
    reply_success "Query successful!", :data => reply_data
  end

end
