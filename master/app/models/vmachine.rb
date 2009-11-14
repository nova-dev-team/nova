class Vmachine < ActiveRecord::Base
  belongs_to :vcluster
  belongs_to :pmachine
  has_many :vmachine_infos


  def master?
    return self == self.vcluster.vmachines.first
  end

  def get_node_list
    list = ""
    self.vcluster.vmachines.each do |vm|
      list += vm.ip + "\t" + vm.hostname + "\n" 
    end
    return list
  end

  def get_package_list
    vc = self.vcluster
    return vc.package_list
  end

  def get_progress

  end

  # return false if faled to allocate the vm to some pmachine
  def start
    pm = Pmachine.start_vm self
    if pm # let Pmachine class do the scheduling
      # if pm returned success
      self.status = "running"
      self.pmachine = pm
      self.save
    else # no available pmachine
    end
    # TODO remember vmachine status
  end

  # TODO stop vmachine
  def stop
    self.pmachine.destroy_vm self if self.pmachine
    self.destroyed = true
    self.status = "not running"
    self.vnc_port = nil
    self.save
  end

  # TODO suspend vmachine
  def suspend
    self.status = "suspended"
    self.pmachine.suspend_vm self
    self.save
  end

  # TODO resume vmachine
  def resume
    self.status = "running"
    self.pmachine.resume_vm self
    self.save
  end

  def Vmachine.all_not_destroyed
    Vmachine.find_all_by_destroyed true
  end

end
