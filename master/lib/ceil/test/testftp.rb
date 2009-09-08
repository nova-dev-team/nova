require File.dirname(__FILE__) + '/../common/ftp'
require File.dirname(__FILE__) + '/../common/dir'
=begin
ftp = FTPTransfer.new('192.168.0.111')

DirTool.make_clean_dir("/tmp/wtf")
ftp.download_dir("/wtf", "/tmp/wtf")
=end

ftp = FTPTransfer.new('192.168.0.110')

DirTool.make_clean_dir("/tmp/keys")
DirTool.make_clean_dir("/tmp/common")
ftp.download_dir("/keys", "/tmp/keys")
ftp.download_dir("/packages/common", "/tmp/common")

#ftp.sucks("/wtf")
