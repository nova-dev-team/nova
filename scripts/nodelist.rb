
def all_node_name
  [
    "node10",
    "node13",
    "node14",
    "node15",
    "node16",
    "node17",
    "node18",
    "node19",
    "node20"
  ].each {|n| yield n}
end

def sys_exec cmd
  puts cmd
  IO.popen(cmd) do |f|
	  puts f.readlines
  end
end


