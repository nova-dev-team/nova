module PmachinesHelper

  class VncPortRange
    attr_accessor :first, :last

    def <=> other
      if other.class == self.class
        if self.first != other.first
          self.first <=> other.first
        else
          self.second <=> other.second
        end
      end
    end
  end

  class Helper

    # test if vnc port not used
    def Helper.vnc_collision? first, last
      first = first.to_i
      last = last.to_i
      test_range = first..last
      Pmachine.all.each do |pmachine|
        if test_range.include? pmachine.vnc_first or test_range.include? pmachine.vnc_last
          return true
        end
      end
      return false
    end

    # automatically detect best vnc ports, for "add new pmachine" dialog
    def Helper.vnc_recommendation
      vnc_start = 5900 # starting port number of vnc
      vnc_segment_size = 10 # 10 vnc ports every segment
      used_vnc = []
      Pmachine.all.each do |pmachine|
        vpr = VncPortRange.new
        vpr.first = pmachine.vnc_first
        vpr.last = pmachine.vnc_last
        used_vnc << vpr
      end
      used_vnc.sort!

      vnc_candidate = vnc_start # the candidate for vnc port 
     
      # find a segment which has not been used
      used_vnc.each do |used|
        candidate_range = vnc_candidate..(vnc_candidate + vnc_segment_size)
        while candidate_range.include? used.first or candidate_range.include? used.last
          vnc_candidate += vnc_segment_size
          candidate_range = vnc_candidate..(vnc_candidate + vnc_segment_size)
        end
      end
      
      "#{vnc_candidate}-#{vnc_candidate + vnc_segment_size - 1}"
    end
  end

end
