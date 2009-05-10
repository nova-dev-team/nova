
require 'rubygems'
require 'ostruct'
require File.join("../../shared-lib/", 'tools')

module U0
  include Tools

  # todo: remove local definition
  SYS_ROOT = '../../level-1-controller/tmp/system'
  SEED_TIME = 1

  DB_URI = '../../level-1-controller/db/development.sqlite3'

  class U0Error < RuntimeError; end
  class U0Retry < RuntimeError; end

  def self.do_download(url, dst)
    LOGGER.debug "u0.do_download ==> #{ url} => #{ dst}"
    path = File.dirname dst
    LOGGER.debug "u0.do_download: aria2c -d #{path} #{url}"

    Signal.trap("USR1") do
      LOGGER.debug "u0.do_download: trap USR1"
      fail '下载文件失败'
    end

    pid = fork {
      `aria2c -d "#{path}" "#{url}"`
      Process.kill("USR1", Process.ppid) if $?.exitstatus != 0
    }
    Process.wait pid
    LOGGER.debug "u0.do_download finish download."
  end

  # torrent下载
  def self.do_bt_download(url, dst)
    LOGGER.debug "u0.do bt download ==> #{url} => #{dst}"
    path = File.dirname dst
    LOGGER.debug "u0.bt download : aria2c --follow-bittorrent=mem --seed-time=#{SEED_TIME} -d #{path} #{url}"

    Signal.trap("USR1") do
      LOGGER.debug "u0.do bt download: trap USR1"
      fail '下载文件失败'
    end

    pid = fork {
      `aria2c --follow-bittorrent=mem --seed-time=#{SEED_TIME} -d "#{path}" "#{url}"`
      Process.kill("USR1", Process.ppid) if $?.exitstatus != 0
    }
    Process.wait pid
    LOGGER.debug "u0.finish bt download."
  end

  ## PUBLIC SERVICES
  ######################

  U0_RETRY = 3

  def self.process(n = U0_RETRY)
    tries = 0
    begin
      tries += 1
      pid = fork do
        yield
      end
      Process.waitpid pid
    rescue U0Retry => e
      if tries < n
        LOGGER.error "u0.process capture a exception #{e}, retry#{2**tries}"
        sleep 2**tries
        retry
      end
    rescue => e
      LOGGER.error "u0.process capture a exception #{e}, abort"
      raise U0Error, e.to_s
    end
  end

  def self.request_download(url, priority, size)
    Tools.dbexec(DB_URI) do |db|
      Tools.sqlite3_insert(db, 'update_image_queues', {:url => url,
                       :priority => priority,
                       :size => size,
                       :created_at => Time.now.to_s,
                       :updated_at => Time.now.to_s})
    end
  end

  def self.get_update_reqs()
    LOGGER.debug '==> U0.get_update_reqs'

    reqs = []
    Tools.dbexec(DB_URI) do |db|
      reqs =  Tools.sqlite3_select(db, 'update_image_queues', [:id, :url], {:progress => '-1'})
    end

    rol = []
    reqs.each { |req|
      r = OpenStruct.new
      r.req_type = 'download'
      r.rowid = req[0]
      r.url = req[1]
      rol << r
    }

    return rol
  end

  def self.process_download(req)
    LOGGER.debug "U0.process_download ==> #{ req.url}"

    uuid = File.basename req.url
    Tools.dbexec(DB_URI) do |db|
      db.transaction
      Tools.sqlite3_update(db, 'update_image_queues', {:progress => 1}, {:url => req.url})
      Tools.sqlite3_insert(db, 'vdisks', {:vd_kind=>'system',
                             :vd_template => uuid,
                             :vd_status => 'creating',
                             :vd_name => uuid,
                             :vd_uuid => uuid,
                             :created_at => Time.now.to_s,
                             :updated_at => Time.now.to_s})
      db.commit
    end

    require 'uri'
    u = URI.parse req.url
    d = File.join(SYS_ROOT, File.basename(u.path))
    if i = d.rindex('torrent', 8)
      do_bt_download u, d
      d = d[0,-9]
    else
      do_download u, d
    end

    Tools.dbexec(DB_URI) do |db|
      db.transaction
      Tools.sqlite3_update(db, 'vdisks', {:vd_status => 'ready'}, {:vd_uuid=> uuid})
      Tools.sqlite3_delete(db, 'update_image_queues', {:url => req.url})
      db.commit
    end
    return d

  rescue => e
    LOGGER.fatal "下载系统镜像失败: #{ e }"
    ## todo: delete queue record too?
    # Tools.sqlite3_delete(db, 'update_image_queues', {:id => reqid[0][0]})
    Tools.dbexec(DB_URI) do |db|
      Tools.sqlite3_update(db, 'update_image_queues', {:progress => -100}, {:url => req.url})
      Tools.sqlite3_delete(db, 'vdisks', {:vd_uuid=> uuid})
    end
    fail U0Error, "下载系统镜像失败"
  end

end
