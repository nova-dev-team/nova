class Vmachine < ActiveRecord::Base
  belongs_to :vcluster
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
  
end
