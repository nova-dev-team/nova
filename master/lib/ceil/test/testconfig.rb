require File.dirname(__FILE__) + "/../server/cluster_config_creator"

nl="10.0.0.2 test1\n10.0.0.3 test2"
il="common\nssh-nopass"
cn="test"
ccc = ClusterConfigurationCreator.new(nl, il, cn)
ccc.create
