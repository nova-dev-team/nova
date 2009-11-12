
def sys_exec cmd
  puts cmd
  IO.popen(cmd) do |f|
    puts f.readlines
  end
end

