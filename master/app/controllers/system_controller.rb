# this controller is used to observer system status
# it could only be triggered by "root"/"admin" group

class SystemController < ApplicationController

  before_filter :root_required

  # show system information
  def index
    render :text => "YOU are in root group, right?"
  end

  def overview
    
  end

  def reconstruct_netpool
    first_ip = params[:first_ip]
    subnet_mask = params[:subnet_mask]
    intranet_device = params[:intranet_device]
    size_mapping = {} # size => count mapping
    pp size_mapping
    size_array = params[:size_array].split.map {|v| v.to_i}
    count_array = params[:count_array].split.map {|v| v.to_i}
    pp size_array
    pp count_array
    (0...size_array.size).each do |i|
      size_mapping[size_array[i]] = count_array[i]
    end
    pp size_mapping
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
