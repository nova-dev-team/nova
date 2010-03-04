require File.join(File.dirname(__FILE__), "kvm/kvm_adapter.rb")

class Virt

  def initialize hypervisor, working_dir
    @hypervisor = hypervisor
    @working_dir = working_dir
  end

end

