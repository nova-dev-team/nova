module Util

  def Util.split_userinfo userinfo
    index = userinfo.index ":"
    username = userinfo[0...index]
    password = userinfo[(index + 1)..-1]
    return username, password
  end

  def split_userinfo userinfo
    Util.split_userinfo
  end

end
