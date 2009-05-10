
module U0
  # todo: remove local definition
  SYS_ROOT = File.join RAILS_ROOT, 'tmp/system'
  SEED_TIME = 1

  class U0Error < RuntimeError; end

  def self.do_download(url, dst)
    puts "do download : #{ url} => #{ dst}"
    path = File.dirname dst
    puts "aria2c -d #{path} #{url}"
    pid = fork { `aria2c -d "#{path}" "#{url}"` }
    Process.wait pid
    puts ">>>do_download ==> #{ $?.exitstatus }"
    fail '下载文件失败' unless $?.exitstatus == 0
    puts "finish download."
  end

  # torrent下载
  def self.do_bt_download(url, dst)
    puts "do download : #{url} => #{dst}"
    path = File.dirname dst
    puts "aria2c --follow-bittorrent=mem --seed-time=#{SEED_TIME} -d #{path} #{url}"
    pid = fork { `aria2c --follow-bittorrent=mem --seed-time=#{SEED_TIME} -d "#{path}" "#{url}"` }
    Process.wait pid
    puts ">>>do_download ==> #{ $?.exitstatus }"
    fail '下载文件失败' unless $?.exitstatus == 0
    puts "finish download."
  end

  ## PUBLIC SERVICES
  ######################

  # Uo.download: a UpdateImageQueue instance => filename
  def U0.download(req)
    puts "U0.download #{ req.url}"

    req.progress = 1
    req.save!

    require 'uri'

    u = URI.parse req.url
    d = File.join(SYS_ROOT, File.basename(u.path))
    if i = d.rindex('torrent', 8)
      do_bt_download u, d
      d = d[0,-9]
    else
      do_download u, d
    end
    return d

  rescue => e
    req.progress = -1
    req.save

    puts "下载系统镜像失败: #{ e }"
    fail U0Error, "下载系统镜像失败"
  end
end
