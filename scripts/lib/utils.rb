require 'yaml'

CONF_YAML = File.dirname(__FILE__) + '/../config/conf.yaml'
NODE_YAML = File.dirname(__FILE__) + '/../config/node.yaml'

def sys_exec cmd
  IO.popen(cmd) do |pipe|
    loop do
      line = pipe.readline
      puts line
      break if pipe.eof?
    end
  end
end

def all_nodes
  y = YAML::load_file NODE_YAML
  y["master"]["role"] = "master"
  list = []
  list << y["master"]
  y["workers"].each do |w|
    w["role"] = "worker"
    list << w
  end
  list.each do |n|
    yield n
  end
end

def all_workers
  all_nodes do |node|
    if node["role"] == "worker"
      yield node
    end
  end
end

def nova_conf
  YAML::load_file CONF_YAML
end

