require 'ftools'

module BatchHelper

  include VclusterHelper

  class Helper

    def Helper.create name, size
      result = {}
      sub_result = VclusterHelper::Helper.create name
      result[:vcluster_cid] = sub_result[:vcluster_cid]
      File.open ("tmp/#{result[:vcluster_cid]}.install.conf", "w") do |file|
        file.write("common\nssh-nopass\n")
        file.flush()
        file.close()
      end

      return result
    end

    # change the settings of the whole cluster
    def Helper.change_setting vcluster_cid, item, value
      # TODO
    end

    def Helper.add_soft vcluster_cid, soft_name
      result = {:success => true}
      File.open ("tmp/#{vcluster_cid}.install.conf", "a") do |file|
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
