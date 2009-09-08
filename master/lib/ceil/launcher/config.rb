require File.dirname(__FILE__) + '/../common/nfs'
require 'net/http'
require 'rubygems'
require 'json'

class ClusterConfiguration
	attr_reader :node_list, :inst_list, 
	            :host_name, :cluster_name, 
	            :package_server, :package_server_type,
	            :key_server, :key_server_type
	
	def initialize(local_addr)
		@node_list = nil
		@inst_list = nil
		@host_name = nil
		@cluster_name = nil
		@local_addr = local_addr
		
		#default server..
		@package_server = "localhost"
		@key_server = "localhost"
    #default server type = nfs
		@package_server_type = "nfs"
		@key_server_type = "nfs"
	end

	def generate_config_file(local_path)
		config_path = local_path + "/config"
		config_file = config_path + "/node.list"

		system "mkdir #{config_path}"

		File.open(config_file, "w") do |file|
			file.puts(@node_list)
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

#cc = ClusterConfiguration.new "192.168.0.110"
#cc.fetch_by_nfs "127.0.0.1"
#puts cc.instlist
#puts cc.nodelist

