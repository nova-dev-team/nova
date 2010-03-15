# A helper controller that provides lots of utilities.
#
# Author::      Santa Zhang (mailto:santa1987@gmail.com)
# Since::       0.3

class MiscController < ApplicationController

  # TODO create a verification image, NOTE that opera's caching causes problems
  def verification_image
    send_file RAILS_ROOT + "/tmp/v.jpg", :type => "image/jpeg", :filename => "v.jpg", :disposition => 'inline'
  end

  # Reply the role of this node.
  #
  # Since:: 0.3
  def role
    reply_success "master"
  end

  def browser_detect
    render :text => (ApplicationHelper::client_browser_name request)
  end

  def echo
    render :text => request.pretty_inspect
  end

  def show_ip
    render :text => request[:ip]
  end


end
