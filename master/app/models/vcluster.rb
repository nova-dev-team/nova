require "ceil_conf"
require "uuidtools"

require "#{CEIL_ROOT}/server/cluster_config_creator"

class Vcluster < ActiveRecord::Base
  has_one :net_segment
  belongs_to :user
  has_many :vmachines
  
  
  #usage
  #
  #   vc = Vcluster.allocate("nova-test-1", "common\nssh-nopass\nmpich2\nhadoop\n", 4)
  #
  #allocate a vcluster named nova-test-1, has 4 nodes(1master/3slaves)
  #then use ceil to install common/ssh-nopass/mpich2/hadoop package
  #
  #return nil if fails
  #   
  #   vc.vmachines.each do |vm|
  #     puts vm.mac      # <---vm's mac must be set to this for dhcp
  #     puts vm.ip + " " + vm.hostname # <--vm info
  #     puts vm.ceil_progress   # <--ceil installation progress, -1 means not started, 100means finished
  #     puts vm.last_ceil_message # <--last message from ceil client on vmachine
  #   end
  #
 	
  def Vcluster.alloc(cluster_name, package_list, machine_count)
    vc = Vcluster.new
    vc.cluster_name = cluster_name
    vc.package_list = package_list
    vc.save
    
    net = NetSegment.alloc(machine_count)
    
    if (!net)
      vc.delete
      return nil
    end
    
    net.vcluster = vc
    net.save

    list = net.list
    node_list = ""
    
    list.each do |node|
      vm = Vmachine.new
      vm.uuid = UUIDTools::UUID.random_create.to_s
      vm.hostname = node[:hostname]
      vm.ip = node[:ip]
      vm.mac = node[:mac]
      vm.vcluster = vc
      vm.save
      
      node_list = node_list + "#{vm.ip}\t#{vm.hostname}\n"
      machine_count -= 1
      break if machine_count <= 0
    end
    
    ccc = ClusterConfigurationCreator.new(node_list, package_list, cluster_name)
    ccc.create
    
    return vc
  end

  ## ensure the vcluster has already requested a network segment.
  ## the request process SHOULD be TRANSACTIONAL
  ## 
  ## if the request fails, raise "no more segment"
  def ensure_net_segment!
    unless self.net_segment
      # TODO allocate net segment
      net_segment = NetSegment.alloc machine_count
      unless net_segment
        raise "no more segment"
      end
      
      node_list = ""
      self.net_segment = net_segment
      (0...self.size).each do |i|
        vm = self.vmachines[i]
        node = net_segment.list[i]

        vm.hostname = node[:hostname]
        vm.ip = node[:ip]
        vm.mac = node[:mac]
        vm.save

        node_list += "#{vm.ip}\t#{vm.hostname}\n"
      end

      ccc = ClusterConfigurationCreator.new node_list, self.package_list, self.name
    end
  end

  ## TODO create a new vcluster according to the params
  def Vcluster.create params
  end

  ## TODO create a new vcluster, and also starts all vmachines of it
  def Vcluster.create_and_start params
  end

  ## TODO destroy this vcluster, and also all vmachines inside it (no going back)
  def destroy!
		self.net_segment.free
		self.destroy
    ## TODO release net segment & allcated vnc
  end

  def Vcluster.all_not_destroyed
    Vcluster.find_all_by_destroyed true
  end

end



