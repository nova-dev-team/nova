#! /usr/bin/ruby

# Automatically create network bridge.
#
# Author::          Huang Gang
# Modified by::     Santa Zhang (mailto:santa1987@gmail.com)
# Since::           0.3

require "#{File.dirname __FILE__}/utils.rb"

require 'yaml'

# Create network bridge.
# * ifname: name of the network interface. (eg, 'eth0')
# * brname: name of the bridge. (eg, 'br0')
#
# Since::         0.3
def create_bridge ifname, brname

  # Check if has some interface.
  #
  # Since::     0.3
  def has_interface name
    pipe = IO.popen "ifconfig | grep ^#{name}"
    pipe_out = pipe.readlines
    pipe.close
    return pipe_out.length != 0
  end

  if has_interface brname
    puts "Warning: bridge '#{brname}' already exists, skip creating"
    return
  end

  local_addr = `ifconfig #{ifname} | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;
  local_mask = `ifconfig #{ifname} | grep Mask | awk \'{print $4}' | awk -F: '{print $2}\'`;
  local_gateway = `route -n | grep #{ifname} | awk '$1 ~ /default|0.0.0.0/ { print $2 }'`
  local_addr = local_addr.chomp
  local_mask = local_mask.chomp
  local_gateway = local_gateway.chomp

  puts "IP Addr = #{local_addr}"
  puts "Netmask = #{local_mask}"
  puts "Default Gateway = #{local_gateway}"

  if local_addr.is_ip_addr? and local_mask.is_ip_addr? and local_gateway.is_ip_addr?
    if has_interface brname
      `ifconfig #{brname} down`
      `brctl delbr #{brname}`
    end
    `brctl addbr #{brname}`
    `brctl setbridgeprio #{brname} 0`
    `brctl addif #{brname} #{ifname}`
    `ifconfig #{ifname} 0.0.0.0`
    `ifconfig #{brname} #{local_addr} netmask #{local_mask}`
    `brctl sethello #{brname} 1`
    `brctl setmaxage #{brname} 4`
    `brctl setfd #{brname} 4`
    `ifconfig #{brname} up`
    `route add default gw #{local_gateway}`
  else
    puts "information about #{ifname} is not available!"
  end
end

# Automatically create network bridge
pipe = IO.popen "ifconfig | grep ^eth"
pipe_out = pipe.readlines
pipe.close

ifname = pipe_out[0].split[0]
puts "Automatically chosen #{ifname} as bridged interface"

conf = YAML::load File.read "#{File.dirname __FILE__}/../../common/config/conf.yml"
brname = conf["vm_network_bridge"]

puts "Creating bridge '#{brname}' on interface '#{ifname}'"

create_bridge ifname, brname

