# This file provides basic utility for worker module.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require 'yaml'
require "#{File.dirname __FILE__}/../../common/lib/utils.rb"

# returns the common config in common/config/conf.yml
def common_conf
  YAML.load File.read("#{File.dirname __FILE__}/../config/conf.yml")
end

