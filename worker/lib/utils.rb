# This file provides basic utility for worker module.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require 'yaml'
require "#{File.dirname __FILE__}/../../common/lib/utils.rb"

class Utils

  class << self

    # return a list of supported hypervisors
    def supported_hypervisors
      conf = YAML.load File.read("#{File.dirname __FILE__}/../../common/config/conf.yml")
      conf["hypervisors"].split ","
    end

  end

end

