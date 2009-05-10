
require 'sdaemon'

module SEngine

  def self.init
    $engine = SDaemon.new
    puts 'Storage Engine Init'
  end

  def self.restart
    return false if $engine.nil?
    $engine.start
    return true
  end

  def self.status
    return false if $engine.nil?
    return $engine.status
  end

  def self.stop
    return false if $engine.nil?
    $engine.stop 
    return true
  end
end
