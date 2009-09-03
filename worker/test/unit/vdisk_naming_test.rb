require 'test_helper'

class VdiskNamingTest < ActiveSupport::TestCase

  include VdiskNaming

  # Replace this with your real tests.
  test "type test" do
    assert (vdisk_type "vd1-iso-winxp.iso") == "iso"
    begin
      vdisk_type "this_will_trigger_error"
    rescue Exception => e
      print e
    end

    assert (vdisk_id "vd2-system-winxp.qcow2") == 2
    begin
      vdisk_id "this_will_trigger_error"
    rescue Exception => e
      print e
    end

    assert (vdisk_cow? "vd2-system-winxp.qcow2") == false
    assert (vdisk_cow? "vd3-user.cow-base.5.qcow2") == true
    assert (vdisk_cow? "vd4-system.cow-base.2.qcow2") == true
    assert (vdisk_cow? "vd5-user-test.qcow2") == false

    assert (vdisk_cow_base "vd3-user.cow-base.5.qcow2") == 5
    assert (vdisk_cow_base "vd4-system.cow-base.2.qcow2") == 2

    begin
      vdisk_cow_base "vd5-user-test.qcow2"
    rescue Exception => e
      print e
    end

    begin
      vdisk_cow_base "this_will_trigger_error"
    rescue Exception => e
      print e
    end
    
  end
end
