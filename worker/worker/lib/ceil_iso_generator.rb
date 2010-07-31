#! /usr/local/bin/ruby

=begin
# Sample usage:
igen = CeilIsoGenerator.new
igen.config_essential('/home/rei/nova/common/lib/ceil')
igen.config_network('10.0.1.198', '255.255.255.0', '10.0.1.254', '166.111.8.28')
igen.config_cluster("nova-0-1", "nova-cluster-name")
igen.config_package_server('10.0.1.223', '8000', 'ftp')
igen.config_key_server('10.0.1.211', '21', 'ftp')
igen.config_nodelist("10.0.1.200 node1\n10.0.1.211 node2")
igen.config_softlist("common ssh-nopass hadoop")
igen.generate('/var/vm1', '/home/rei/test.iso')
=end

require "../../common/lib/ceil/server/ceil_iso_generator.rb"

