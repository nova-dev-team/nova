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
