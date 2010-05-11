require 'test_helper'

class VmachineTest < ActiveSupport::TestCase
  # Replace this with your real tests.
  test "the truth" do
    assert true
  end

  test "log file creation" do
    vm_name = "dummy_vm_for_test"
    Vmachine.log vm_name, "Testing"
    assert File.exist? "#{Setting.vm_root}/#{vm_name}/log"
  end

end
