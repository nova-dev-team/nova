
module DirTool
	def DirTool.temp_dir(suffix)
		note = `date "+%Y%m%d%H%M"`.chomp
		suff = rand(1000)
		return "/tmp/#{note}_#{suff}_#{suffix}"
	end

  def DirTool.make_clean_dir(local_path)
    system "rm -rf #{local_path} 2> /dev/null"
    system "mkdir #{local_path}"
  end

  def DirTool.mkdir(local_path)
    system "mkdir \"#{local_path}\" 2> /dev/null"	
  end

	def DirTool.backup(file_path)
    note = `date "+%Y%m%d%H%M"`.chomp
    system "mv #{file_path} #{file_path}.backup.#{note}"
	end

end

