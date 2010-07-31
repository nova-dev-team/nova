#! /usr/local/bin/ruby
#packing ceil iso
# iso content =
#    ceil_base_path/* + node.list + soft.list + network.conf + server.conf

require 'fileutils'
require File.dirname(__FILE__) + '/../common/ip'

#require File.dirname(__FILE__) + '/../common/dir'


CEIL_ISO_FILENAME_SERVERS = 'servers.conf'
CEIL_ISO_FILENAME_NETWORK = 'network.conf'
CEIL_ISO_FILENAME_CLUSTER = 'cluster.conf'
CEIL_ISO_FILENAME_NODELIST = 'node.list'
CEIL_ISO_FILENAME_SOFTLIST = 'soft.list'

CEIL_ISO_CONFIG_PATH = '/config'
CEIL_ISO_PACKAGE_PATH = '/packages'
CEIL_ISO_KEY_PATH = '/keys'
CEIL_RHEL5_CONFIG_PATH = '/rhel5'

#require 'dir'

PARAM_GENISO = ' -allow-lowercase -allow-multidot -D -L -f -l -o '
PATH_GENISO = '/usr/bin/genisoimage'
PATH_MKISO = '/usr/bin/mkisofs'

CONFIG_PATH = CEIL_ISO_CONFIG_PATH
PACKAGE_PATH = CEIL_ISO_PACKAGE_PATH
KEY_PATH = CEIL_ISO_KEY_PATH
RHEL5_CONFIG_PATH = CEIL_RHEL5_CONFIG_PATH

FILENAME_SERVERS = CEIL_ISO_FILENAME_SERVERS
FILENAME_NETWORK = CEIL_ISO_FILENAME_NETWORK
FILENAME_CLUSTER = CEIL_ISO_FILENAME_CLUSTER
FILENAME_NODELIST = CEIL_ISO_FILENAME_NODELIST
FILENAME_SOFTLIST = CEIL_ISO_FILENAME_SOFTLIST

class CeilIsoGenerator
  def initialize
    @net_ipaddr = nil
    @net_netmask = nil
    @net_gateway = nil
    @net_dns = nil

    @base_path = nil #ceil_base_path

    @server_package = nil
    @server_key = nil #ipaddr of package/key server

    @port_package = nil
    @port_key = nil

    @server_type_key = nil
    @server_type_package = nil #server type, ie. FTP, NFS, BLAH..

    @nodelist = nil
    #nodelist string

    @softlist = nil
    @hostname = 'nova'
    @clustername = 'nova-cluster'
    @id_rsa_content = nil
    @id_rsa_pub_content = nil

    @changelist_username = []
    @changelist_new_pwd = []

    #softlist string: appnames seperated by space
    #example
    # softlist = "hg hj hx hz"
  end

  def s(str)
    if str == nil
      return ""
    else
      return str
    end
  end

  def config_ssh_key(id_rsa_content, id_rsa_pub_content)
    @id_rsa_content = id_rsa_content
    @id_rsa_pub_content = id_rsa_pub_content
  end

  def config_passwd(username, new_pwd)
    @changelist_username << username;
#		@changelist_origin_pwd << origin_pwd;
    @changelist_new_pwd << new_pwd;
  end

  def config_essential(ceil_base_path)
    @base_path = ceil_base_path
  end

  def config_network(ipaddr, netmask, gateway, nameserver)
    @net_ipaddr = ipaddr
    @net_netmask = netmask
    @net_gateway = gateway
    @net_dns = nameserver
  end

  def config_package_server(server_package, port_package, server_type_package)
    @server_package = server_package
    @port_package = port_package
    @server_type_package = server_type_package
  end

  def config_key_server(server_key, port_key, server_type_key)
    @server_key = server_key
    @port_key = port_key
    @server_type_key = server_type_key
  end

  def config_nodelist(nodelist)
    @nodelist = nodelist		
  end

  def config_softlist(softlist)
    @softlist = softlist
  end

  def config_cluster(hostname, clustername)
    @hostname = hostname
    @clustername = clustername
  end

  def generate(tmp_path, iso_path)
    #	1.get tmp_path
    # 2.cp files to tmp dir
    # 3.geniso
    tmpdir = tmp_path
    try_count = 0

=begin
    begin
      #tmpdir = DirTool.temp_dir("iso#{try_count}")
      try_count = try_count + 1
      #puts try_count
      result = 0
      begin
        Dir.mkdir(tmpdir)
      rescue SystemCallError
        result = 1
      end
    end until (result == 0 || try_count > 3)

    if try_count > 3
      puts "Cannot create tempdir"
      return nil
    end
=end

    # link base_path to tmpdir/
    #FileUtils.cp_r(@base_path, tmpdir)
    begin	
      FileUtils.ln_s(@base_path, tmpdir)
    rescue
    end

    # create config files
    begin
      FileUtils.mkdir_p(tmpdir + CONFIG_PATH)
      #FileUtils.mkdir_p(tmpdir + PACKAGE_PATH)
      FileUtils.mkdir_p(tmpdir + KEY_PATH)
      FileUtils.mkdir_p(tmpdir + RHEL5_CONFIG_PATH)
    rescue
    end

    rhel5_path = File.join(tmpdir, RHEL5_CONFIG_PATH)

    filename_servers = tmpdir + CONFIG_PATH + '/' + FILENAME_SERVERS
    filename_network = tmpdir + CONFIG_PATH + '/' + FILENAME_NETWORK
    filename_nodelist = tmpdir + CONFIG_PATH + '/' + FILENAME_NODELIST
    filename_softlist = tmpdir + CONFIG_PATH + '/' + FILENAME_SOFTLIST
    filename_cluster = tmpdir + CONFIG_PATH + '/' + FILENAME_CLUSTER

    File.open(filename_servers, 'w') do |file|
      content = ""
      content << s(@server_package) << "\n"
      content << s(@port_package) << "\n"
      content << s(@server_type_package) << "\n"
      content << s(@server_key) << "\n"
      content << s(@port_key) << "\n"
      content << s(@server_type_key) << "\n"
      file.puts content
    end

    File.open(filename_network, 'w') do |file|
      content = ""
      content << s(@net_ipaddr) << "\n"
      content << s(@net_netmask) << "\n"
      content << s(@net_gateway) << "\n"
      content << s(@net_dns) << "\n"
      file.puts content
    end

    File.open(filename_nodelist, 'w') do |file|
      file.puts s(@nodelist)
    end

    File.open(filename_softlist, 'w') do |file|
      file.puts s(@softlist)
    end

    File.open(filename_cluster, 'w') do |file|
      file.puts s(@hostname)
      file.puts s(@clustername)
    end

    if @id_rsa_content
      sshkey_path = tmpdir + KEY_PATH + '/ssh-nopass'
      begin
        FileUtils.mkdir_p(sshkey_path)
      rescue
      end
      File.open(sshkey_path + '/id_rsa', 'w') do |file|
        file.puts s(@id_rsa_content)
      end
      File.open(sshkey_path + '/id_rsa.pub', 'w') do |file|
        file.puts s(@id_rsa_pub_content)
      end

      attach_filename = File.dirname(__FILE__) + '/packages/ssh-nopass'
      attach_destname = sshkey_path + '/attach.sh'	
      begin
        FileUtils.cp(attach_filename, attach_destname)
      rescue
      end
    end

    if @changelist_username.length > 0
      passwd_path = tmpdir + KEY_PATH + '/passwd'
      attach_filename = File.dirname(__FILE__) + '/packages/passwd'
      attach_destname = passwd_path + '/attach.sh'	

      expect_filename = File.dirname(__FILE__) + '/packages/pwd.exp'
      expect_destname = passwd_path + '/pwd.exp'	
      begin
        FileUtils.mkdir_p(passwd_path)
      rescue
      end

      File.open(passwd_path + '/passwd.list', 'w') do |file|
        0.upto(@changelist_username.length - 1) do |i|
          file.puts s(@changelist_username[i])
          file.puts s(@changelist_new_pwd[i])
        end
      end
      begin
        FileUtils.cp(attach_filename, attach_destname)
      rescue
      end
      begin
        FileUtils.cp(expect_filename, expect_destname)
      rescue
      end

    end


    ## generate well-done config-file for rhel5
    # these files will be copied directly into vm's sysconfig folder
    # by ceil scripts

    # /etc/sysconfig/network-scripts/ifcfg-eth0
    # rhel5_path/ifcfg-eth0
    File.open(File.join(rhel5_path, 'ifcfg_eth0'), 'w') do |f|
      content = <<IFCFG
DEVICE=eth0
BOOTPROTO=static
BROADCAST=#{IpV4Address.calc_broadcast(@net_ipaddr, @net_netmask)}
IPADDR=#{@net_ipaddr}
NETMASK=#{@net_netmask}
NETWORK=#{IpV4Address.calc_network(@net_ipaddr, @net_netmask)}
ONBOOT=yes
IFCFG
      f.puts content
    end

    # /etc/resolv.conf
    File.open(File.join(rhel5_path, 'resolv.conf'), 'w') do |f|
      @net_dns.split(" ").each do |dns|
        f.puts "nameserver #{dns}"
      end
    end

    # /etc/sysconfig/network
    File.open(File.join(rhel5_path, 'network'), 'w') do |f|
      content = <<NCFG
NETWORKING=yes
HOSTNAME=#{@hostname}
GATEWAY=#{@net_gateway}
NCFG
      f.puts content
    end

    #id_rsa & id_rsa.pub
    #authorized_keys
    if @id_rsa_content
      File.open(File.join(rhel5_path, 'id_rsa'), 'w') do |f|
        f.write s(@id_rsa_content)
      end
      File.open(File.join(rhel5_path, 'id_rsa.pub'), 'w') do |f|
        f.write s(@id_rsa_pub_content)
      end
      File.open(File.join(rhel5_path, 'authorized_keys'), 'w') do |f|
        f.write s(@id_rsa_pub_content)
      end
    end

    #change passwd scripts
    #cfg-passwd
    if @changelist_username.length > 0
      File.open(File.join(rhel5_path, 'passwd.list'), 'w') do |f|
        0.upto(@changelist_username.length - 1) do |i|
          f.puts s(@changelist_username[i])
          f.puts s(@changelist_new_pwd[i])
        end
      end
    end

    #/etc/hosts
    if @nodelist
      File.open(File.join(rhel5_path, 'node.list'), 'w') do |f|
        f.puts @nodelist
      end
    end

    #3.pack tmpdir
    generator = nil
    if File.exists? PATH_GENISO
      generator = PATH_GENISO
    elsif File.exists? PATH_MKISO
      generator = PATH_MKISO
    end

    if generator
      cmdline = generator + PARAM_GENISO + iso_path + " " + tmpdir + " 2> /dev/null";
    else
      cmdline = "exit 1"
    end
    puts cmdline
    result = system cmdline
    if result
      return iso_path
    else
      return nil
    end
  end
end

=begin
igen = CeilIsoGenerator.new

igen.config_essential('/nova/system/common/lib/ceil')
igen.config_network('10.0.1.122', '255.255.255.0', '10.0.1.254', '166.111.8.28 166.111.8.29')
# vm_addr  vm_netmask vm_gateway vm_nameserver
igen.config_cluster("node1", "nova-cluster-name")
# vm_nodename  vm_clustername

igen.config_package_server('santa:santa@10.0.1.223', '8000', 'ftp')

igen.config_key_server('santa:santa@10.0.1.223', '8000', 'ftp')

igen.config_nodelist("10.0.1.122 node1\n10.0.1.211 node2")

igen.config_softlist("common passwd ssh-nopass hadoop")
#passwd == change user passwd
#ssh-nopass == deploy ssh-key

igen.config_passwd("root", "remi")
igen.config_passwd("rei", "remi")
#config for package "passwd"

igen.config_ssh_key("jklfdsjkljailgjweklgjklwdjgkl;d", "fsdkhgklsdad;gjdkslgjsdkl;gjsdklgjkl;g")
#config for package "ssh-nopass"
#private_key_content, public_key_content

igen.generate('/tmp/vm1/', '/tmp/jk.iso')

puts "fin"
=end

