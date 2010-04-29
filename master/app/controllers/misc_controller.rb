# A helper controller that provides lots of utilities.
#
# Author::      Santa Zhang (mailto:santa1987@gmail.com)
# Since::       0.3

class MiscController < ApplicationController

  # Reply the role of this node.
  #
  # Since::   0.3
  def role
    reply_success "master"
  end

  # Reply the role of current user.
  # Possible return values: "root", "admin", "normal user". If user not logged in, an failure will be returned.
  #
  # Since::   0.3
  def my_privilege
    if logged_in?
      priv = "root"
      reply_success "Your privilege is '#{priv}'", :privilege => priv
    else
      reply_failure "You are not logged in!"
    end
  end

end
