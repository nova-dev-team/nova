# This file contains general utilities for the Nova system.
# It is shared among "master" & "worker" modules, and the helper scripts.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3


# Reopens string class, and added some helper functions.
#
# Since::     0.3
class String

  # Check if the string itself looks like an uuid.
  #
  # Since::   0.3
  def is_uuid?
    return false unless self.length == 36
    self.downcase.each_char {|ch| return false unless (ch == '-' or ('0' <= ch and ch <= '9') or ('a' <= ch and ch <= 'f'))}
    return false unless ((self.split '-').map {|segment| segment.length}) == [8, 4, 4, 4, 12]
    true
  end

  # Check if the string looks like ip address.
  #
  # Since::   0.3
  def is_ip_addr?
    return false if (self.count ".") != 3
    splt = self.split "."
    return false if splt.length != 4
    splt.each do |seg|
      return false if seg == ""
      return false if seg.to_i.to_s != seg
    end
    true
  end

end

# Define a module which could be displayed as file size.
#
# Since::     0.3
module CouldDisplayAsFileSize

  # Convert current value to file size. Largest unit is TB. Returned value is a String.
  #
  # Since::       0.3
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
    else # largest unit is TB
      val = pretty_val(self / 1024.0 / 1024.0 / 1024.0 / 1024.0)
      return val.to_s + "T"
    end
  end
end

# Reopens the Fixnum class, and add some helper function to it.
#
# Since::     0.3
class Fixnum
  include CouldDisplayAsFileSize
end

# Reopens the Bignum class, and add some helper function to it.
#
# Since::     0.3
class Bignum
  include CouldDisplayAsFileSize
end


# Execute a shell command, prints the command it self, and the output message.
#
# Since::     0.3
def my_exec cmd
  cmd.each_line do |cmd_line|
    cmd_line = cmd_line.strip
    next if cmd_line == ""
    puts "[cmd] #{cmd_line}"
  end
  system cmd
end


# Check if running with root privilege, exit 1 if not.
# This function is intended to be used in Rakefile.
#
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

# Randomly generate a token with alphabets, like "acd", 'zfc', etc.
#
# Since::     0.3
def random_token length = 5
  token = ""
  alphabets = "abcdefghijklmnopqrstuvwxyz"
  1.upto(length) do |i|
    idx = rand(alphabets.length)
    token += alphabets[idx..idx]
  end
  return token
end


# A collection of helpers for manipulating IP address.
#
# Since::    0.3
module IpTools

  # Convert an ipV4 address to int value.
  #
  # Since::     0.3
  def IpTools.ipv4_to_i ip_str
    splt = ip_str.split "."
    ((splt[0].to_i * 256 + splt[1].to_i) * 256 + splt[2].to_i) * 256 + splt[3].to_i
  end

  # Convert an ipV4 address to bits array.
  # The loweset bit will be at the beginning of the bits array.
  #
  # Since::     0.3
  def IpTools.ipv4_to_bits ip_str
    bits_array = []
    ival = IpTools.ipv4_to_i ip_str
    1.upto(32) do
      b = ival % 2
      bits_array << b
      ival = ival / 2
    end
    bits_array
  end

  # Convert an bits arary back to ip address.
  #
  # Since::   0.3
  def IpTools.bits_to_ipv4 bits_array
    ival = 0
    w = 1
    bits_array.each do |b|
      ival += b * w
      w *= 2
    end
    return IpTools.i_to_ipv4 ival
  end

  # Convert an integer to ipV4 address.
  #
  # Since::     0.3
  def IpTools.i_to_ipv4 i_val
    ip_seg = []
    1.upto(4) do |n|
      ip_seg << (i_val % 256)
      i_val = i_val / 256
    end
    ip_seg = ip_seg.reverse
    return ip_seg.join "."
  end

  # Get the last ip address in the subnet.
  #
  # Since::   0.3
  def IpTools.last_ip_in_subnet first_ip, submask
    first_ip_bits = IpTools.ipv4_to_bits first_ip
    submask_bits = IpTools.ipv4_to_bits submask
    last_bits = []
    (0..31).each do |i|
      if submask_bits[i] == 0
        last_bits << 1
      else
        last_bits << first_ip_bits[i]
      end
    end
    IpTools.bits_to_ipv4 last_bits
  end

end

