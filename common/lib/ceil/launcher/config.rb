require File.dirname(__FILE__) + '/../common/nfs'
require File.dirname(__FILE__) + '/../common/iso'

require 'net/http'
require 'rubygems'
require 'json'

class ClusterConfiguration
  attr_reader :node_list, :inst_list, 
              :host_name, :cluster_name, 
              :package_server, :package_server_type, :package_server_port,
							:package_server_username, :package_server_password,
							:key_server_username, :key_server_password,
              :key_server, :key_server_type, :key_server_port, 
							:character, :local_addr, :local_mask, :default_gateway, :name_server

  def initialize(local_addr)
  	@local_addr = nil
		@local_mask = nil
		@default_gateway = nil
		@name_server = nil

    @node_list = nil
    @inst_list = nil
    @host_name = nil
    @cluster_name = nil
    @local_addr = local_addr
	
    #default params
    @package_server = "localhost"
    @key_server = "localhost"

    @package_server_type = "ftp"
		@package_server_port = '21'

    @key_server_type = "ftp"
		@key_server_port = '21'
		@package_server_username = 'anonymous'
		@package_server_password = 'CeilClient'
		@key_server_username = 'anonymous'
		@key_server_password = 'CeilClient'

		@character = ["worker"]
  end

  def generate_config_file(local_path)
    config_path = local_path + "/config"
    config_file = config_path + "/node.list"

    system "mkdir #{config_path}"

    File.open(config_file, "w") do |file|
      file.puts(@node_list)
    end
  end

	def fetch_by_iso
		iso_root = File.dirname(__FILE__) + '/../..'
		path_config = iso_root + CEIL_ISO_CONFIG_PATH
		filename_servers_config = path_config + '/' + CEIL_ISO_FILENAME_SERVERS
		filename_cluster_config = path_config + '/' + CEIL_ISO_FILENAME_CLUSTER
		filename_soft_list = path_config + '/' + CEIL_ISO_FILENAME_SOFTLIST
		filename_node_list = path_config + '/' + CEIL_ISO_FILENAME_NODELIST
		filename_network_config = path_config + '/' + CEIL_ISO_FILENAME_NETWORK
		begin
			File.open(filename_network_config, 'r') do |file|
				@local_addr = file.readline.chomp
				@local_mask = file.readline.chomp
				@default_gateway = file.readline.chomp
				@name_server = file.readline.chomp
			end

			File.open(filename_node_list) do |file|
				@node_list = file.readlines
			end	
			@node_list = @node_list.join.chomp
			master = @node_list.split[0]
		
			if master == @local_addr
				@character << "master"
			end

			File.open(filename_soft_list) do |file|
				@inst_list = file.readlines
			end
			@inst_list = @inst_list.join.chomp



			File.open(filename_servers_config) do |file|
				package_server = file.readline.chomp.split '@'
				@package_server = package_server[-1]
				package_server_login = package_server[0].split ':'

				@package_server_username = package_server_login[0]
				@package_server_password = package_server_login[-1]

				@package_server_port = file.readline.chomp
				@package_server_type = file.readline.chomp

				key_server = file.readline.chomp.split '@'
				@key_server = key_server[-1]
				key_server_login = key_server[0].split ':'
				@key_server_username = key_server_login[0]
				@key_server_password = key_server_login[-1]

				@key_server_port = file.readline.chomp
				@key_server_type = file.readline.chomp
			end
		

			File.open(filename_cluster_config) do |file|
				@host_name = file.readline.chomp
				@cluster_name = file.readline.chomp
			end

		rescue => e
			puts "Error during fetch configuration from cdrom, #{e.to_s}"
			return nil
		end
	end


  def fetch_by_nfs(server_addr)
    map_source = "/config"
    nfs = NFSTransfer.new(server_addr)
    nfs.mount(map_source) do |map_path|
      map_list = map_path + "/map.list"

      File.open(map_list, "r") do |file|
        file.each_line do |line|
          ip, cname = line.split
          if ip == @local_addr
            puts "found! #{@local_addr}"
            @cluster_name = cname
          end
        end
      end

      puts "final = #{@cluster_name}"
      if @cluster_name != ""
        node_list = "#{map_path}/#{@cluster_name}/node.list"
        inst_list = "#{map_path}/#{@cluster_name}/inst.list"
        @node_list = `cat #{node_list}`.chomp
        @inst_list = `cat #{inst_list}`.chomp
        @node_list.each_line do |line|
          ip, nname = line.split
          @host_name = nname if ip == @local_addr
        end
      end
    end
    return @cluster_name
  end

  def fetch_by_http(server_addr)
    begin
      conn = Net::HTTP.new(server_addr, 3000)
      resp, cjson = conn.get("/ceil/retrieve.json", nil)
    rescue => e
      puts "Error during fetch configuration through HTTP, #{e.to_s}"
      return nil
    end

    begin		
      conf = JSON.parse(cjson)
    rescue => e
      puts "Error during parsing JSON TEXT, #{e.to_s}"
      return nil
    end

    @package_server = conf["package_server"] if conf["package_server"] 
    @package_server_type = conf["package_server_type"] if conf["package_server_type"]
    @key_server = conf["key_server"] if conf["key_server"]
    @key_server_type = conf["key_server_type"] if conf["key_server_type"]

    return nil if !(@host_name = conf["host_name"])
    return nil if !(@node_list = conf["node_list"])
    return nil if !(@inst_list = conf["package_list"])
    return nil if !(@cluster_name = conf["cluster_name"])

    @cluster_name
  end
end

=begin
cc = ClusterConfiguration.new "10.0.1.211"
cc.fetch_by_iso
puts cc.inst_list
puts cc.node_list
=end

