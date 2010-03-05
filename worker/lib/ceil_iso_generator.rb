#! /usr/local/bin/ruby
#packing ceil iso
# iso content = 
#    ceil_base_path/* + node.list + soft.list + network.conf + server.conf
require 'fileutils'
#require 'dir'
	
PARAM_GENISO = ' -allow-lowercase -allow-multidot -D -L -l -o '
PATH_GENISO = '/usr/bin/genisoimage'

FILENAME_SERVERS = 'servers.conf'
FILENAME_NETWORK = 'network.conf'
FILENAME_NODELIST = 'node.list'
FILENAME_SOFTLIST = 'soft.list'

class CeilIsoGenerator
	def temp_dir(suffix)
		note = `date "+%Y%m%d%H%M"`.chomp
		suff = rand(1000)
		return "/tmp/#{note}_#{suff}_#{suffix}"
	end

	def initialize
		@net_ipaddr = nil
		@net_netmask = nil
		@net_gateway = nil
		@net_dns = nil

		@base_path = nil #ceil_base_path

		@server_package = nil
		@server_key = nil #ipaddr of package/key server

		@server_type_key = nil
		@server_type_package = nil #server type, ie. FTP, NFS, BLAH..

		@nodelist = nil 
		#nodelist string

		@softlist = nil
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

	def config_servers(server_package, server_type_package, server_key, server_type_key)
		@server_package = server_package
		@server_type_package = server_type_package
		@server_key = server_key
		@server_type_key = server_type_key
	end

	def config_nodelist(nodelist) 
		@nodelist = nodelist		
	end

	def config_softlist(softlist)
		@softlist = softlist
	end

	def generate(iso_path)

		# 1.mk tmp dir /tmp/geniso
		# 2.cp files to tmp dir
		# 3.geniso
    tmpdir = nil
		try_count = 0

		begin
			tmpdir = temp_dir("iso#{try_count}")
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

		# copy files
		FileUtils.cp_r(@base_path, tmpdir)
		# create config files
		filename_servers = tmpdir + '/' + FILENAME_SERVERS
		filename_network = tmpdir + '/' + FILENAME_NETWORK
		filename_nodelist = tmpdir + '/' + FILENAME_NODELIST
		filename_softlist = tmpdir + '/' + FILENAME_SOFTLIST

		File.open(filename_servers, 'w') do |file|
			content = ""
			content << @server_package << '\n'
			content << @server_type_package << '\n'
			content << @server_key << '\n'
			content << @server_type_key << '\n'
			file.puts content
		end

		File.open(filename_network, 'w') do |file|
			content = ""
			content << @net_ipaddr << '\n'
			content << @net_netmask << '\n'
			content << @net_gateway << '\n'
			content << @net_dns << '\n'
			file.puts content
		end

		File.open(filename_nodelist, 'w') do |file|
			file.puts @nodelist
		end

		File.open(filename_softlist, 'w') do |file|
			file.puts @softlist
		end

		#3.pack tmpdir
		cmdline = PATH_GENISO + PARAM_GENISO + iso_path + " " + tmpdir;
		result = system cmdline
		if result 
			return iso_path
		else
			return nil
		end
	end

end

=begin
example:

igen = CeilIsoGenerator.new
igen.config_essential('/home/rei/nova/tools')
igen.config_network('10.0.1.210', '255.255.255.0', '10.0.1.254', '166.111.8.28')
igen.config_servers('10.0.1.215', 'FTP', '10.0.1.215', 'NFS')
igen.config_nodelist('10.0.1.210 node1\n10.0.1.211 node2')
igen.config_softlist('common ssh-nopass hadoop')
igen.generate('/home/rei/test.iso')
=end


