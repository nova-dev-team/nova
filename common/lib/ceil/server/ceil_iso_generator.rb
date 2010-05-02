#! /usr/local/bin/ruby
#packing ceil iso
# iso content = 
#    ceil_base_path/* + node.list + soft.list + network.conf + server.conf
require 'fileutils'

#require File.dirname(__FILE__) + '/../common/dir'

#require File.dirname(__FILE__) + '/../common/iso'
CEIL_ISO_FILENAME_SERVERS = 'servers.conf'
CEIL_ISO_FILENAME_NETWORK = 'network.conf'
CEIL_ISO_FILENAME_CLUSTER = 'cluster.conf'
CEIL_ISO_FILENAME_NODELIST = 'node.list'
CEIL_ISO_FILENAME_SOFTLIST = 'soft.list'

CEIL_ISO_CONFIG_PATH = '/config'

#require 'dir'
	
PARAM_GENISO = ' -allow-lowercase -allow-multidot -D -L -f -l -o '
PATH_GENISO = '/usr/bin/genisoimage'

CONFIG_PATH = CEIL_ISO_CONFIG_PATH

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

		#softlist string: appnames seperated by space
		#example
		# softlist = "hg hj hx hz"
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

		FileUtils.ln_s(@base_path, tmpdir)
		# create config files
		FileUtils.mkdir(tmpdir + CONFIG_PATH)
		#DirTool.mkdir()

		filename_servers = tmpdir + CONFIG_PATH + '/' + FILENAME_SERVERS
		filename_network = tmpdir + CONFIG_PATH + '/' + FILENAME_NETWORK
		filename_nodelist = tmpdir + CONFIG_PATH + '/' + FILENAME_NODELIST
		filename_softlist = tmpdir + CONFIG_PATH + '/' + FILENAME_SOFTLIST
		filename_cluster = tmpdir + CONFIG_PATH + '/' + FILENAME_CLUSTER

		File.open(filename_servers, 'w') do |file|
			content = ""
			content << @server_package << "\n"
			content << @port_package << "\n"
			content << @server_type_package << "\n"
			content << @server_key << "\n"
			content << @port_key << "\n"
			content << @server_type_key << "\n"
			file.puts content
		end

		File.open(filename_network, 'w') do |file|
			content = ""
			content << @net_ipaddr << "\n"
			content << @net_netmask << "\n"
			content << @net_gateway << "\n"
			content << @net_dns << "\n"
			file.puts content
		end

		File.open(filename_nodelist, 'w') do |file|
			file.puts @nodelist
		end

		File.open(filename_softlist, 'w') do |file|
			file.puts @softlist
		end
		
		File.open(filename_cluster, 'w') do |file|
			file.puts @hostname
			file.puts @clustername
		end

		#3.pack tmpdir
		cmdline = PATH_GENISO + PARAM_GENISO + iso_path + " " + tmpdir + " 2> /dev/null";
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
igen.config_essential('/home/rei/nova/common/lib/ceil')
igen.config_network('10.0.1.200', '255.255.255.0', '10.0.1.254', '166.111.8.28')
igen.config_cluster("nova-0-1", "nova-cluster-name")
igen.config_package_server('santa:santa@10.0.1.223', '8000', 'ftp')
igen.config_key_server('santa:santa@10.0.1.223', '8000', 'ftp')
igen.config_nodelist("10.0.1.200 node1\n10.0.1.211 node2")
igen.config_softlist("common ssh-nopass hadoop")
igen.generate('/var/vm1', '/home/rei/test.iso')
=end

