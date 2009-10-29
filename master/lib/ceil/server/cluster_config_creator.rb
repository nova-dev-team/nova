require File.dirname(__FILE__) + '/../common/dir'
require File.dirname(__FILE__) + '/server_settings'
require File.dirname(__FILE__) + '/dispatch_key'

class ClusterConfigurationCreator
	def initialize(node_list, inst_list, cluster_name)
		@node_list = node_list
		@inst_list = inst_list
		@cluster_name = cluster_name
	end

	def create
		config_path = "#{SERVER_CONFIG_STORE_PATH}/#{@cluster_name}"
		disp_path = "#{SERVER_KEY_DISPATCH_PATH}/#{@cluster_name}"

		node_list_file = "#{config_path}/node.list"
		inst_list_file = "#{config_path}/inst.list"

# old code for NFS configuration server
    begin
      #DirTool.make_clean_dir(config_path)
      DirTool.make_clean_dir(disp_path)

      #File.open(node_list_file, "w") do |file|
      #  file.puts(@node_list)
      #end
      #File.open(inst_list_file, "w") do |file|
      #  file.puts(@inst_list)
      #end

      @inst_list.each_line do |line|
        app_name = line.chomp
        kd = KeyDispatcher.new(@cluster_name, app_name)
        kd.dispatch(@node_list)
      end
    rescue Exception => e
      print "Error writing NFS settings in Ceil component!"
      # not a serious error if you are not using NFS to aid installation
    end

	end
end

# usage
#nl="192.168.0.116 ubuntu-c01\n192.168.0.121 ubuntu-c02"
#il="common\nssh-nopass"
#cn="vm2test"

#ccc = ClusterConfigurationCreator.new(nl, il, cn)
#ccc.create

