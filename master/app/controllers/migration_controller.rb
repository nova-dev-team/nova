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
        :ip => pm.ip, :hostname => pm.hostname, :vm_capacity => pm.vm_capacity,
        :status => pm.status
      }
      vm_all_data = []
      pm.vmachines.each do |vm|
        # only running/suspended vmachines could be migrated
        next unless vm.status == "running" or vm.status == "suspended"
        # TODO add info of vm migration, such as migrate_from, migrate_to, etc.
        vm_data = {
          :name => vm.name,
          :uuid => vm.uuid
        }
        vm_all_data << vm_data
      end
      pm_data[:vmachines] = vm_all_data
      reply_data << pm_data
    end
    reply_success "Query successful!", :data => reply_data
  end

  def live_migrate
    unless valid_param? params["vm_uuid"]
      reply_failure "Please provide the 'vm_uuid' parameter!"
      return
    end
    unless valid_param? params["dest_ip"]
      reply_failure "Please provide the 'dest_ip' parameter!"
      return
    end
    unless params["dest_ip"].to_s.is_ip_addr?
      reply_failure "The 'dest_ip' is '#{params["dest_ip"]}, which is not a valid IP address!'"
      return
    end
    pm = Pmachine.find_by_ip params["dest_ip"]
    if pm == nil
      reply_failure "Pmachine with IP='#{params["dest_ip"]}' not found!"
      return
    end
    unless pm.status == "working"
      reply_failure "Could only migrate to working pmachine!"
      return
    end
    vm = Vmachine.find_by_uuid params["vm_uuid"]
    if vm == nil
      reply_failure "Vmachine with UUID='#{params["vm_uuid"]}' not found!"
      return
    end
    unless vm.status == "running"
      reply_failure "Only running vmachine could be migrated!"
      return
    end
    if vm.live_migrate_to params["dest_ip"]
      reply_success "The live migration will be done in a few seconds."
    else
      reply_failure "Failed to prepare the migration work!"
    end
  end

end
