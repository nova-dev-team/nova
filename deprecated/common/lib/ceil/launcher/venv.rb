require File.dirname(__FILE__) + '/../common/ip'
require File.dirname(__FILE__) + '/../common/dir'
require File.dirname(__FILE__) + '/../common/iso'

class Environment
  attr_reader :local_addr, :local_mask, :server_addr, :default_gateway, :name_server;

   def initialize
    @local_addr = nil
    @local_mask = nil
    @server_addr = nil

    @default_gateway = nil
    @name_server = nil

  end

  def check_iso
    iso_root = File.dirname(__FILE__) + '/../..'
    path_config = iso_root + CEIL_ISO_CONFIG_PATH
    filename_network_config = path_config + '/' + CEIL_ISO_FILENAME_NETWORK

    puts filename_network_config
    begin
      File.open(filename_network_config, 'r') do |file|
        @local_addr = file.readline.chomp
        @local_mask = file.readline.chomp
        @default_gateway = file.readline.chomp
        @name_server = file.readline.chomp
      end

      puts @local_addr
      puts @local_mask
      puts @default_gateway
      puts @name_server
      puts "FIN"
      return @local_addr

    rescue
      puts "Cannot find #{CEIL_ISO_CONFIG_PATH}/#{CEIL_ISO_FILENAME_NETWORK} in cdrom!"
      return nil
    end
  end

  def reconfig_network_win32
    filename_ipchanger = File.dirname(__FILE__) + '/ipchanger.exe'
    cmd = "#{filename_ipchanger} #{@local_addr} #{@local_mask} #{@default_gateway} #{@name_server}"
    #puts cmd
    system cmd
  end

  def reconfig_network(if_name)
    ubuntu_network_interface = '/etc/network/interfaces'
    ubuntu_network_nameserver = '/etc/resolv.conf'
    content = ""
    content << "auto lo\n"
    content << "iface lo inet loopback\n"
    content << "\n"
    content << "auto #{if_name}\n"
    content << "iface #{if_name} inet static\n"
    content << "	address #{@local_addr}\n"
    content << "	netmask #{@local_mask}\n"
    content << "	gateway #{@default_gateway}\n"
    content << "\n"
    content << "# #{`date`}\n"

    DirTool.backup(ubuntu_network_interface)
    File.open(ubuntu_network_interface, 'wb') do |file|
      file.puts(content)
    end

    DirTool.backup(ubuntu_network_nameserver)
    File.open(ubuntu_network_nameserver, 'wb') do |file|
      file.puts("nameserver #{@name_server}")
    end

    system "ifdown #{if_name}"
    sleep 5
    system "ifup #{if_name}"
    sleep 10
  end

  def check
    local_addr = `ifconfig eth0 | grep "inet addr" | awk \'{print $2}\' | awk -F: \'{print $2}\'`;
    local_mask = `ifconfig eth0 | grep Mask | awk \'{print $4}' | awk -F: '{print $2}\'`;

    local_addr = local_addr.chomp
    local_mask = local_mask.chomp

    if IpV4Address::ip?(local_addr) && IpV4Address::ip?(local_addr)
      @local_addr = local_addr;
      @local_mask = local_mask;
      @server_addr = IpV4Address::calc_gateway(local_addr, local_mask);
    else
      puts "cannot fetch correct information for eth0!"
      return nil
    end
  end


  def check_win
    require 'socket'
    @local_addr = IPSocket.getaddress(Socket.gethostname)
    @server_addr = IpV4Address::calc_prev(@local_addr)
  end
end

=begin
env = Environment.new
env.check_iso
puts env.local_addr
=end

#env = Environment.new;
#env.check;
#puts env.local_ip;
#puts env.local_mask;
#puts env.server_ip;


