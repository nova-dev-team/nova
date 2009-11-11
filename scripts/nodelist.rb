
def all_node_name
  [
    "ubuntu",
    "ubuntu1"
  ].each {|n| yield n}
end

def sys_exec cmd
  puts cmd
  IO.popen(cmd) do |f|
    puts f.readlines
  end
end


