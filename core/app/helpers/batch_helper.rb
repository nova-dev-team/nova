require 'ftools'
require 'net_pool'

module BatchHelper

  include VclusterHelper
  include VmachineHelper

  class Helper

    def Helper.create name, size
      result = {}
      size = size.to_i
      sub_result = VclusterHelper::Helper.create name
      result[:vcluster_cid] = sub_result[:vcluster_cid]
      File.open("tmp/#{result[:vcluster_cid]}.install.conf", "w") do |file|
        file.write("common\nssh-nopass\n")
        #file.flush
        #file.close
      end

      vcluster = Vcluster.find_by_id result[:vcluster_cid][1..-1]
      n_pool = NetPool.alloc(size)

      if n_pool == nil
        # alloc failed
        result[:success] = false
        return
      end

      vcluster.net_pool_name = n_pool[0]
      vcluster.save
      result[:net_pool_name] = vcluster.net_pool_name

      result[:vm_settings] = []

      n_pool[1].each do |net_addr|
        vmachine = Vmachine.create
        vmachine.settings =<<HERE
{"mem":512, "img":"", "vcpu":1, "mac":"#{net_addr[2]}", "ip":"#{net_addr[1]}", "hostname":"#{net_addr[0]}"}
HERE
#        result[:vm_settings] << vmachine.settings
        vmachine.save
      end

      File.open("tmp/#{result[:vcluster_cid]}.nodelist.conf", "w") do |file|
        n_pool[1].each do |net_addr|
          file.write("#{net_addr[1]} #{net_addr[0]}\n")
        end
      end

      result[:success] = true

      return result
    end

    # change the settings of the whole cluster
    def Helper.change_setting vcluster_cid, item, value
      # TODO
    end

    def Helper.add_soft vcluster_cid, soft_name
      result = {:success => true}
      File.open("tmp/#{vcluster_cid}.install.conf", "a") do |file|
        file.write("#{soft_name}\n")
      end
#      result[:soft_list] = []
#      File.foreach ("tmp/#{vcluster_cid}.install.conf") do |line|
#        result[:soft_list] << line.chomp
#      end
      return result
    end

    def Helper.do_install vcluster_cid
      result = {}
      # TODO write node.list and call hg
      return result
    end

    def Helper.progress vcluster_cid
      result = {}
# TODO call hg
      return result
    end

  end
end
