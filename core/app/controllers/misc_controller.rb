## class that handles miscellaneous actions
class MiscController < ApplicationController

  # TODO creats a verification image
  def verification_image
    
    send_file RAILS_ROOT + "/public/images/check10x10.gif", :type => "image/gif", :filename => "a.gif", :disposition => 'inline'


  end

  def hi
    render :text => "HI"
  end

end
