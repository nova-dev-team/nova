# The model for virtual machines.
#
# Author::    Santa Zhang
# Since::     0.3

class Vmachine < ActiveRecord::Base

  belongs_to :vcluster
  belongs_to :pmachine
  has_many :vmachine_infos

  # Get the hostname of the VM
  #
  # Since::   0.3
  def hostname
    vc = self.vcluster
    vc_name_length = vc.cluster_name.length
    self.name[(vc_name_length + 1)..-1]
  end

  # live migrate the VM to another worker machine
  #
  # Since::   0.3
  def live_migrate_to dest_ip
    if self.pmachine == nil
      return false
    else
      self.migrate_to = dest_ip
      self.migrate_from = self.pmachine.ip
      self.save
    end
  end

  # Write a log about current VM
  # Params:
  #   catetory: the category for the log
  #   message: detailed info of the log
  #
  # Since::   0.3
  def log category, message
    info = VmachineInfo.new
    info.category = category
    info.message = message
    info.vmachine = self
    info.save
  end

end
