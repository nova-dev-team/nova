class String
  def is_uuid?
    return false unless self.length == 36
    self.downcase.each_char {|ch| return false unless (ch == '-' or ('0' <= ch and ch <= '9') or ('a' <= ch and ch <= 'f'))}
    return false unless ((self.split '-').map {|segment| segment.length}) == [8, 4, 4, 4, 12]
    true
  end
end

class Fixnum

  def to_pretty_file_size
    def pretty_val val
      if val > 10
        val.round
      else
        (val * 10.0).round / 10.0
      end
    end

    if self < 1000
      return self.to_s
    elsif self < 1000 * 1000
      val = pretty_val(self / 1024.0)
      return val.to_s + "K"
    elsif self < 1000 * 1000 * 1000
      val = pretty_val(self / 1024.0 / 1024.0)
      return val.to_s + "M"
    elsif self < 1000 * 1000 * 1000 * 1000
      val = pretty_val(self / 1024.0 / 1024.0 / 1024.0)
      return val.to_s + "G"
    end
  end

end


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


module VdiskNaming

  def VdiskNaming.vdisk_type filename
    begin
      split = filename.split "-"
      return split[1]
    rescue
      raise "'#{filename}' is not in correct form!"
    end
  end

  # return a string representing the type of vdisk
  # could be "iso", "empty", "system", "system.cow", "user", "user.cow"i
  def vdisk_type filename
    VdiskNaming.vdisk_type filename
  end

  def vdisk_id filename
    begin
      split = filename.split "-"
      return split[0][2..-1].to_i
    rescue
      raise "'#{filename}' is not in correct form!"
    end
  end

  # return nil in case of failure
  def vdisk_cow? filename
    begin
      split = filename.split "-"
      return split[1].end_with? "cow"
    rescue
      raise "'#{filename}' is not in correct form!"
    end
  end

  def vdisk_cow_base filename
    raise "'#{filename}' is not Copy-On-Write!" unless vdisk_cow? filename

    begin
      split = filename.split "-"
      split_again = split[2].split "."
      return split_again[1].to_i
    rescue
      raise "'#{filename}' is not in correct form!"
    end
  end

end

