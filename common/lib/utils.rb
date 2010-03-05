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

  def VdiskNaming.system_disk? filename
    (VdiskNaming.vdisk_type filename) == "sys" or (VdiskNaming.vdisk_type filename) == "sys.cow"
  end

  def VdiskNaming.vdisk_type filename
    begin
      split = filename.split "-"
      return split[1]
    rescue
      raise "'#{filename}' is not in correct form!"
    end
  end

  # return a string representing the type of vdisk
  # could be "iso", "sys", "sys.cow", "usr", "usr.cow"
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


# Execute a shell command, prints the command it self, and the output message.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3
def my_exec cmd
  puts "[cmd] #{cmd}"
  system cmd
end


# Check if running with root privilege, exit 1 if not.
# This function is intended to be used in Rakefile.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3
def require_root_privilege
  if ENV['USER'] != "root"
    puts "This script requires root privilege!"
    exit 1
  end
end


# Kill a process by pid file.
# This function is intended to be used in Rakefile.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3
def kill_by_pid_file file_path
  if File.exists? file_path
    puts "Found pid file: #{file_path}"
    pid = File.read file_path
    puts "Process pid: #{pid}"
    puts "Terminating..."
    my_exec "kill -9 #{pid}"
    File.delete file_path
  end
end

