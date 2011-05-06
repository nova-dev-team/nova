require File.dirname(__FILE__) + '/dir'

class NFSTransfer
  def initialize(server_addr)
    @server_addr = server_addr
  end

  def mount(remote_path)
    temp_path = DirTool.temp_generate("nfs_#{remote_path.hash.to_s}")
    begin
      DirTool.make_clean_dir(temp_path)
      system "umount -f #{temp_path} 2\> /dev/null"
      system "mount #{@server_addr}:#{remote_path} #{temp_path}"

      yield temp_path
    rescue
      puts "oh somebody has set up us the bomb"
    ensure
      system "umount -f #{temp_path}"
    end

  end

  def download_dir(remote_path, local_path)
    mount(remote_path) do |temp_path|
      system "cp -r #{temp_path}/* #{local_path}"
    end
  end

  def touch(remote_path, filename)
    mount(remote_path) do |temp_path|
      system "touch #{temp_path}/filename"
    end
  end
end



