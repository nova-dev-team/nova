class Vmachine < ActiveRecord::Base
  belongs_to :vcluster
  belongs_to :pmachine
  has_one :vnc_port
  has_many :vmachine_infos
  
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
    pm = Pmachine.start_vm self # let Pmachine class do the scheduling
    if pm != nil
      # if pm returned success
      #   self.status = "running"
    else # no available pmachine
    end
    # TODO remember vmachine status
  end

  # TODO stop vmachine
  def stop
    if self.status == "running"
      self.pmachine.stop_vm self
      self.destroyed = true
      self.save
    end
  end

  # TODO suspend vmachine
  def suspend
  end

  # TODO resume vmachine
  def resume
  end

  def Vmachine.all_not_destroyed
    Vmachine.find_all_by_destroyed true
  end
  
end
