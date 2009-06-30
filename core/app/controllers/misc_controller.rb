## class that handles miscellaneous actions
class MiscController < ApplicationController

  # TODO creat a verification image
  def verification_image
    send_file RAILS_ROOT + "/tmp/v.jpg", :type => "image/jpeg", :filename => "v.jpg", :disposition => 'inline'
  end

  def hi
    render :text => "HI"
  end

end
