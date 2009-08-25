require 'test_helper'
require 'pp'

class NetSegmentTest < ActiveSupport::TestCase
  # Replace this with your real tests.
  test "the truth" do
    assert true
  end

  test "deleting_vcluster_assoc" do
=begin 
    req = { 4 => 1 }
    dev = "eth1"
    segment_begin = "10.0.15.0"
    segment_mask = "255.255.255.0"
    
    
    NetSegment._reconstruct(segment_begin, segment_mask, req, dev)
    
    #blah = Vcluster.new
    #nn = NetSegment.new
    #blah.net_segment = nn
    #nn.save
    #pp nn
    #pp blah
    #blah.net_segment.free
    #pp nn
    #pp blah
    #pp blah.net_segment
    #pp "OH CRAPPY TEST"
    
    #pp nn.test
=end
    
  end
end
