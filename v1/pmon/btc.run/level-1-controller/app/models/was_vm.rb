
require 'rubygems'
#require 'xmlsimple'
require 'rexml/document'


class Vm

  # level 1
  attr_accessor :name, :uuid, :memory, :vcpu, :clock
  # level 1
  attr_accessor :os

  # level 2 (device)
  attr_accessor :emulator, :disks, :interfaces, :graphics, :os

  # non-libvirt attributes 
  attr_accessor :format, :define, :desc

  # format => :libvirt | ...
  # data => define
  def initialize(args)
    if args[:format] == 'libvirt'
      @define = args[:data]
      @domain = REXML::Document.new @define
      _update_level0
      _update_emualtor
      _update_disks
      _update_interfaces
      _update_graphics
      _update_os
    end
  end

  def getxml
    return @define
  end

  protected

  def _update_level0
    @name = REXML::XPath.first @domain, "/domain/name/text()"
    @memory = REXML::XPath.first @domain, "/domain/memory/text()"
    @vcpu = REXML::XPath.first @domain, "/domain/vcpu/text()"
    @uuid = REXML::XPath.first @domain, "/domain/uuid/text()"
  end

  def _update_os
    @os_arch = REXML::XPath.first @domain, "/domain/os/text()"
    @os_type = REXML::XPath.first @domain, "/domain/os/type/text()"
  end
  
  def _update_emualtor
    @os_type = REXML::XPath.first @domain, "/domain/devices/emulator/text()"
  end
  
  def _update_disks
  end
  
  def _update_interfaces
  end
  
  def _update_graphics
  end
  
end
