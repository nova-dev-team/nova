# 存储管理引擎

require 'rubygems'
require 'sqlite3'
require 'uuidtools'
require File.join("../../shared-lib/", 'tools')
require 'ostruct'

module S0
  include Tools

  LOCAL_PATH = '../../level-1-controller/tmp/local'
  SYSTEM_PATH = '../../level-1-controller/tmp/system'
  DB_URI = '../../level-1-controller/db/development.sqlite3'
  VD_CACHE = 5

  RETRY_TIME = 60

  class S0Error < RuntimeError; end
  class S0Retry < RuntimeError; end


  # 建立本地镜像文件，并格式化.
  def self.do_create(filename, size, format)
    puts "create ......"
    `touch #{ filename }`
    sleep 5
    puts "...... created "
  end

  # 克隆文件
  def self.do_clone_file(new, old)
    LOGGER.debug "do_clone_file ==> #{ new } = #{ old }"

    Signal.trap("USR1") do
      LOGGER.debug "s0.do_clone_file: trap USR1"
      fail S0Error, '复制文件失败'
    end

    pid = fork {
      `cp #{old} #{new}`
      Process.kill("USR1", Process.ppid) if $?.exitstatus != 0
    }
    Process.wait pid
    LOGGER.debug "finish cloning."
  end

  # 拷贝文件方法
  def self.do_delete_file(filename)
    LOGGER.debug "do_delete_file => #{ filename }"

    Signal.trap("USR1") do
      LOGGER.debug "s0.do_delete_file: trap USR1"
      fail '删除文件失败'
    end

    fork {
      `rm -f #{filename}`
      Process.kill("USR1", Process.ppid) if $?.exitstatus != 0
    }
    Process.wait

    LOGGER.debug "finish delete."
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

# == Schema Information
# Schema version: 20090403015007
#
# Table name: vdisks
#
#  id           :integer         not null, primary key
#  vd_kind      :string(255)     not null
#  vd_template  :string(255)
#  vd_status    :string(255)
#  vd_name      :string(255)
#  vd_uuid      :string(255)     not null
#  vm_uuid      :string(255)
#  lock_version :integer         default(0)
#  created_at   :datetime
#  updated_at   :datetime
#

  S0_RETRY = 3

  def self.process(n = S0_RETRY)
    tries = 0
    begin
      tries += 1
      pid = fork do
        yield
      end
      Process.waitpid pid
    rescue S0Retry => e
      if tries < n
        LOGGER.error "s0.process capture a exception #{e}, retry#{2**tries}"
        sleep 2**tries
        retry
      end
    rescue => e
      LOGGER.error "s0.process capture a exception #{e}, abort"
      raise S0Error, e.to_s
    end
  end

  def self._filename(req)
    if req.vd_type == 'system'
      return File.join(SYSTEM_PATH, req.vd_uuid)
    else
      return File.join(LOCAL_PATH, req.vd_uuid)
    end
  end

  def self._local_filename(id)
    return File.join(LOCAL_PATH, id)
  end

  def self.process_clone(req)
    LOGGER.debug "==> S0.process_clone : #{req}"

    uuid = UUID.random_create.to_s

    Tools.dbexec(DB_URI) do |db|
      Tools.sqlite3_insert(db, 'vdisks', {:vd_kind=>'local',
                             :vd_template => req.vd_uuid,
                             :vd_status => 'creating',
                             :vd_name => uuid,
                             :vd_uuid => uuid,
                             :created_at => Time.now.to_s,
                             :updated_at => Time.now.to_s})
    end

    do_clone_file(_local_filename(uuid), _filename(req))


    Tools.dbexec(DB_URI) do |db|
      db.transaction
      res = Tools.sqlite3_select(db, 'vdisks', ["id"], {:vd_uuid => uuid})
      Tools.sqlite3_update_by_id(db, 'vdisks', {:vd_status => 'ready'}, res[0][0])
      db.commit
    end

  rescue => e
    LOGGER.debug "process_clone capture a exeception: #{e}, abort"
    Tools.dbexec(DB_URI, 0) do |db|
      Tools.sqlite3_delete(db, 'vdisks', {:vd_uuid => uuid})
    end
    raise S0Error, e.to_s
  end

  def self.process_delete(req)
    LOGGER.debug "==> S0.process_delete : #{req}"

    lovd = []
    Tools.dbexec(DB_URI) do |db|
      db.transaction
      lovd = Tools.sqlite3_select(db, 'vdisks', [:vd_uuid], {:vm_uuid => req.vm_uuid})
      if lovd.length == 0
        LOGGER.fatal "there are not existed vdisks for vm #{req.vm_uuid}!"
        return
      end
      Tools.sqlite3_update(db, 'vdisks', {:vd_status => 'delete'}, {:vm_uuid => req.vm_uuid})
      db.commit
    end

    lovd.each { |vd|
      do_delete_file(_local_filename(vd[0]))
    }

    Tools.dbexec(DB_URI) do |db|
      Tools.sqlite3_delete(db, 'vdisks', {:vm_uuid => req.vm_uuid})
    end

  rescue => e
    LOGGER.debug "process_delete capture a exeception: #{e}, abort"
    raise S0Error, e.to_s
  end

  def self.use_a_vdisk(vd_uuid, vm_uuid)
    LOGGER.debug "S0.use_a_vdisk ==> #{vd_uuid}, #{vm_uuid}"

    Tools.dbexec(DB_URI) do |db|
      svd = Tools.sqlite3_select(db, 'vdisks', ["id"], {:vd_uuid => vd_uuid})
      fail(S0Error, 'system image is not exites.') if svd.length == 0

      db.transaction
      vds = Tools.sqlite3_select(db,
                                 'vdisks',
                                 ["id", :vd_uuid],
                                 {:vd_template => vd_uuid, :vd_kind => 'local', :vd_status => 'ready'})
      fail(S0Error, '没有足够的存储资源了，早没了') if vds.length == 0

      vds.each do |vd|
        Tools.sqlite3_update_by_id(db, 'vdisks', {:vd_status => 'used', :vm_uuid => vm_uuid}, vd[0]) rescue next
        db.commit
        return vd[1]
      end
    end

    fail S0Error, '没有足够的存储资源了, 刚都被抢了'
  end

  def self.create_del_vd_req(vm_uuid)
    LOGGER.debug "S0.create_del_vd_req ==> #{vm_uuid}"

    req = OpenStruct.new
    req.req_type = 'delete'
    req.vd_type = 'local'
    req.vm_uuid = vm_uuid
    req
  end

  def self.create_clone_reqs()
    LOGGER.debug '==> S0.get_clone_reqs'

    res = []

    Tools.dbexec(DB_URI) do |db|
      sysvds =  Tools.sqlite3_select(db, 'vdisks', [:id, :vd_uuid], {:vd_kind => 'system', :vd_status => 'ready'})
      sysvds.each { |svd|
        vds = Tools.sqlite3_select(db, 'vdisks', [:id], {:vd_template => svd[1]})
        (VD_CACHE - vds.length).times {
          r = OpenStruct.new
          r.req_type = 'clone'
          r.vd_type = 'system'
          r.rowid = svd[0]
          r.vd_uuid = svd[1]
          res << r
        }
      }
    end

    return res
  end

end

