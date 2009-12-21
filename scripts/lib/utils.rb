require 'yaml'
require 'socket'
require 'fileutils'

NOVA_ROOT = File.dirname(__FILE__) + "/../../"
CONF_YAML = "#{NOVA_ROOT}/common/config/conf.yaml"
NODE_YAML = "#{NOVA_ROOT}/common/config/node.yaml"
TEMP_DIR = File.dirname(__FILE__) + "/../tmp/"

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

def ensure_temp_dir_exists
  FileUtils.mkdir_p TEMP_DIR
end

def current_git_branch
    IO.popen("git branch") do |f|
	f.each_line do |line|
	    if line.start_with? "*"
		splt = line.split
		return splt[1]
	    end
	end
    end
end

def is_git_master_branch?
    return current_git_branch == "master"
end

def git_update_this_node
    repo = "http://166.111.131.32/nova-update.git"
    working_branch = current_git_branch
    if working_branch != "master"
	sys_exec "git checkout master"
    end

    sys_exec "git pull #{repo} master"

    if working_branch != "master"
	sys_exec "git checkout #{working_branch}"
    end
end

