
require 's0'
require "dbus"
require File.join('../shared-lib/' + "utils")

module XHelper
  include Utils

  # 根据规格建立临时存储
  #   request, specification =>
  # 是一个hash对象，包括关键字：:src, :uuid, :size, :format等。
  # 下面这个请求通过拷贝模板建立本地存储
  #   {:src=>"copy", :uuid=>"xxx-yyy-zzz-uuuu"}
  # 下面这个请求通过新建本地存储
  #   {:src=>"new", :size=>"500m", :format=>"ext3"}
  def create_storage_for_vm(req, spec)
    logger.debug "x::create_storage_for_vm: #{ req }, #{ spec }"

    if spec[:src].downcase == 'copy' and spec.key?(:uuid)
      puts "#{ req.to_yaml }#{ spec }"
      #      vd = S0.get spec[:uuid], req.uuid
      vd_uuid = call_bus('/cn/org/btc/StorageEngine') do |bus|
        bus.get spec[:uuid], req.uuid
      end
      return nil if vd_uuid[0] == ''
      vd = Vdisk.find_by_vd_uuid(vd_uuid[0])
      reset_storage_ready req.uuid unless vd
    elsif spec[:src].downcase == 'new' and spec.key?(:size) and spec.key?(:format)
      vd = S0.create spec
    else
      fail ArgumentError, "不能识别的虚拟机存储构造类型"
    end
    return vd
  end

  def prepare_storage(req, xml)
    lod = REXML::XPath.match xml, '/domain/devices/disk/source'
    lod.each do |d|
      # todo: 校验spec合法
      spec = d.attribute('file').value
      puts ">>>>>" + spec
      vd = create_storage_for_vm req, eval(spec)
      fail S0::S0Error unless vd
      d.add_attribute('file', vd.filename)
    end
  end

  def reset_storage_ready(vm_uuid)
    Vdisk.find(:all, :conditions => { :vm_uuid => vm_uuid}).each { |vd|
      vd.vd_status = 'ready'
      vd.vm_uuid = nil
      vd.save!
    }
  end

end
