class NetPool < ActiveRecord::Base

  protected

  def self._return_list(net, size)
    list = []
    ip = Utils::IPCalc.new(net.begin, net.mask)
    size.times do |n|
      list << [net.name + "_#{n}", ip.value, ip.mac ]
      ip.next
    end
    return list
  end

  public

  # 请求分配一个网络.
  #   size => array of (hostname, ip, mac)
  def NetPool.alloc(size)
    find(:all, :conditions => { :used => false}).each do |net|
      if net.size >= size
        net.used = true
        net.save! rescue next
        return net.name, _return_list(net, size)
      end
    end
  end

  # 释放一个网络
  #   name, size =>
  def NetPool.free(name)
    net = find_by_name name
    net.used = 'false'
    net.save!
  end
end

