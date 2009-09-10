# this controller is used to observer system status
# it could only be triggered by "root"/"admin" group

class SystemController < ApplicationController

  before_filter :root_required

  # show system information
  def index
    render :text => "YOU are in root group, right?"
  end

  def reconstruct_netpool
    first_ip = "10.0.2.3"
    subnet_mask = "255.255.255.0"
    size_mapping = {
      # size => count
      3 => 4,
      2 => 2,
      17 => 3
    }
    intranet_device = "eth0"
    NetSegment._reconstruct(first_ip, subnet_mask, size_mapping, intranet_device)
    result = {
      :dhcpdconf => (whole_file_content "#{RAILS_ROOT}/tmp/dhcpd.conf"),
      :interfaces => (whole_file_content "#{RAILS_ROOT}/tmp/interfaces")
    }
    render_data result
  end

private

  def whole_file_content filename
    file = File.open filename
    lines = file.readlines
    lines.inject("\n") {|a, b| a + b}
  end

  def root_required
    redirect_to login_url unless logged_in? and current_user.in_group? "root"
  end
  
end
