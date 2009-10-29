
module DirTool
  def DirTool.temp_generate(suffix)
    note = `date "+%Y%m%d%H%M"`.chomp
    return "/tmp/#{note}_#{suffix}"
  end

  def DirTool.make_clean_dir(local_path)
    system "rm -rf #{local_path} 2> /dev/null"
    system "mkdir #{local_path}"
  end

  def DirTool.mkdir(local_path)
    system "mkdir \"#{local_path}\" 2> /dev/null"	
  end
end

