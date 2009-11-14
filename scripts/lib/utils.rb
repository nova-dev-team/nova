require 'yaml'
require 'socket'

NOVA_ROOT = File.dirname(__FILE__) + "/../../"
CONF_YAML = File.dirname(__FILE__) + '/../config/conf.yaml'
NODE_YAML = File.dirname(__FILE__) + '/../config/node.yaml'

def sys_exec cmd
  IO.popen(cmd) do |pipe|
    loop do
      line = pipe.readline unless pipe.eof?
      puts line unless pipe.eof?
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

def this_node
  this_node_ip = IPSocket.getaddress Socket.gethostname
  all_nodes do |node|
    if node["role"] == "master"
      return node if node["intranet_ip"] == this_node_ip or node["internet_ip"] == this_node_ip
    elsif node["role"] == "worker"
      return node if node["intranet_ip"] == this_node_ip
    end
  end
  return nil
end

def nova_conf
  YAML::load_file CONF_YAML
end

