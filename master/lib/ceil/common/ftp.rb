require 'net/ftp'
require File.dirname(__FILE__) + '/../common/rescue'
class String
	def dir?
		return (self[-1] == 47)  #string ends with "/" is a dir
	end
end

class Net::FTP
	def nlist(dir = "")
		cmd = "NLST -b #{dir}"
		files = []
		retrlines(cmd) do |line|
			files.push(line)
		end
		return files
	end

	def cd(dir)
  	begin
  	  self.chdir(dir)
	  rescue => e
	    puts "[#{dir}] is a File, #{e.to_s}"
	    return nil
	  end
	  return 0
	end
end

class FTPTransfer
	def initialize(server_addr)
		BasicSocket.do_not_reverse_lookup = true
		@server_addr = server_addr
	end

	def download_file(remote_path, file_name, local_path)
		#puts "		DOWNLOAD #{remote_path}/#{file_name} #{local_path}"
		Rescue.ignore {
			@conn.getbinaryfile(remote_path + "/" + file_name, local_path + "/" + file_name)
		}
	end

	def download_recursive(remote_path, local_path)
		#puts "DIR #{remote_path}"
		DirTool.mkdir(local_path)
		Rescue.ignore {
		  return nil if !@conn.cd(remote_path) #chdir to remote_path
			files = @conn.nlist
			files.each do |file|
				puts "	FILE: #{file}"
				if @conn.cd(remote_path + "/" + file)
				  puts "[#{file}] is a dir, now dip into it"
					download_recursive(remote_path + "/" + file, local_path + "/" + file)
				else
				  puts "[#{file}] is a file, now download #{remote_path}/#{file} to #{local_path}"
					download_file(remote_path, file, local_path)
				end
			end
		}
	end
	def download_dir(remote_path, local_path)
		puts "DOWN DIR #{remote_path}"
		@conn = Net::FTP.new(@server_addr)

		Rescue.ignore {
  		@conn.login("anonymous", "ceil_client")
  	}

		@conn.passive = true

		Rescue.ignore {
			download_recursive(remote_path, local_path)
		}
		@conn.close
	end
end



