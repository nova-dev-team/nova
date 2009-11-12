def sys_exec cmd
  puts cmd
  IO.popen(cmd) do |f|
    puts f.readlines
  end
end

def all_nodes
  list = []
  File.open(File.dirname(__FILE__) + "/../config/node.list", "r") do |f|
    list = f.readlines
  end
  list.each do |line|
    if line.index "#"
      line = line[0..(line.index "#") - 1] # skip comments
    end
    splt = line.split
    if splt.length == 2
      host = splt[0]
      role = splt[1]
      yield host, role
    end
  end
end

def all_workers
  all_nodes do |host, role|
    if role == "worker"
      yield host
    end
  end
end
