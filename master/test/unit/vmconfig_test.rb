require 'test_helper'
require 'pp'

class VmachineTest < ActiveSupport::TestCase


  test "test fetch vm config" do
    
    vc = Vcluster.new()
    vc.cluster_name = "XQ"
    vc.package_list = "common\nssh-nopass\n"
    
    v1 = Vmachine.new()
    
    v1.uuid = "blah"
    
    v1.ip = "10.0.10.2"
    v1.mac = "33:44:55:66:77:88"
    v1.hostname = "gundam1"
    
    v1.save
    
    v2 = Vmachine.new()
    
    v2.uuid = "halb"
    v2.ip = "10.0.10.3"
    v2.mac = "44:55:66:77:88:99"
    v2.hostname = "gundam2"
    v2.save
    
    vc.vmachines << v1
    vc.vmachines << v2
    vc.save    
    
    ip = "10.0.10.3"
    vm = Vmachine.find(:all, :conditions => { :ip => ip}).first
    pp "1"
    pp vm
    pp "2"
    puts vm.get_node_list
    pp "3"
    puts vm.get_package_list
    pp "end"
    
  end
end
