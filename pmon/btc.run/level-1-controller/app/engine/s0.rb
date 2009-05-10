# 存储管理引擎
module S0
  require 'rubygems'
  require 'uuidtools'

  RETRY_TIME = 60

  class S0Error < RuntimeError; end

  # 建立本地镜像文件，并格式化.
  def self.do_create(filename, size, format)
    puts "create ......"
    `touch #{ filename }`
    sleep 5
    puts "...... created "
  end

  # 拷贝文件方法
  def self.do_clone_file(new, old)
    puts "cloning ...... #{ new } = #{ old }"
    pid = fork { `cp #{old} #{new}` }
    Process.wait pid
    puts ">>>do_clone_file ==> #{ $?.exitstatus }"
    fail '复制文件失败' unless $?.exitstatus == 0
    puts "finish cloning."
  end

  # 拷贝文件方法
  def self.do_delete_file(filename)
    puts "deleting ...... #{ filename }"
    fork { `rm -f #{filename}` }
    Process.wait
    puts ">>>do_delete_file ==> #{ $?.exitstatus }"
    fail '删除文件失败' unless $?.exitstatus == 0
    puts "finish delete."
  end

  def self.add_sys_img filename
    puts ">>>>>> #{ filename }"
    nvd = Vdisk.new
    nvd.vd_kind = 'system'
    nvd.vd_status = 'ready'
    nvd.vd_uuid = File.basename filename
    nvd.vd_template = nil
    nvd.vd_name = filename
    nvd.save!
  end

  ### PUBLIC SERVICE
  #########################

  # 获取一个系统镜像
  #   S0.get: uuid of system image, uuid of a vm => the vdisk
  def S0.get(vd_uuid, vm_uuid)
    puts "S0.get: #{vd_uuid}, #{vm_uuid}"

    fail(S0Error, 'system image is not exites.') unless
      Vdisk.find_by_vd_uuid_and_vd_kind vd_uuid, 'system'

    local_vds = Vdisk.find_all_by_vd_template_and_vd_status vd_uuid, 'ready'
    fail(S0Error, '没有足够的存储资源了，早没了') if local_vds.length == 0

    local_vds.each do |vd|
      vd.vm_uuid = vm_uuid
      vd.vd_status = 'used'
      vd.save! rescue next
      return vd
    end
    fail S0Error, '没有足够的存储资源了, 刚都被抢了'
  end

  # 从系统镜像生成一个本地镜像。
  #    clone the uuid of vdisk of system image, =>
  def S0.clone(svd)
    puts "S0.clone #{svd.vd_uuid}"

    nvd = Vdisk.new
    nvd.vd_uuid = UUID.random_create.to_s
    nvd.vd_template = svd.vd_uuid
    nvd.vd_status = 'creating'
    nvd.vd_kind = 'local'

    nvd.save!

    do_clone_file nvd.filename, nvd.template_filename

    nvd.vd_status = 'ready'

    nvd.save!

  rescue => e
    nvd.destroy if defined?(nvd) and not nvd.nil?
    puts "#{ e }"
    fail S0Error, "克隆系统映像失败"
  end

  def S0.delete(vd)
    puts "S0.delete #{ vd.vd_uuid}"
    do_delete_file vd.filename
    vd.destroy
  end
end

