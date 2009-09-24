
def all_node_name
  [
    "node8",
    "node9",
    "node10",
    "node13",
    "node14",
    "node15",
    "node16",
    "node17",
    "node18",
    "node19"
  ].each {|n| yield n}
end

def sys_exec cmd
  puts cmd
  IO.popen(cmd) do |f|
	  puts f.readlines
  end
end


