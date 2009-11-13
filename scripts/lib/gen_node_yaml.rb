require 'yaml'
require 'pp'
require File.dirname(__FILE__) + "/utils.rb"

def gen_node_yaml
  master = nil
  workers = []
  should_gen = false # should we really generate the node_list
  
  if File.exist? NODE_YAML
    puts "Existing 'node.yaml' detected"
    all_nodes do |node|
      master = node if node["role"] == "master"
      workers << node if node["role"] == "worker"
    end
  end

  puts "Configuring 'node.yaml' file"

  if master != nil:
    puts "Existing master node info:"
    master.each do |key, value|
      puts " * #{key} = #{value}"
    end
    print "Are you going to modify it?(y/N):"
    if STDIN.gets.chomp.downcase.start_with? "y"
      master = nil
    else
      puts "Master node info not changed"
    end
  end

  if master == nil
    master = {}
    loop do
      puts "Please input master node info:"
      print "Hostname: "
      master["hostname"] = STDIN.gets.chomp

      print "Internet IP: "
      master["internet_ip"] = STDIN.gets.chomp

      print "Intranet IP: "
      master["intranet_ip"] = STDIN.gets.chomp

      puts ""
      master.each do |key, value|
	puts " * #{key} = #{value}"
      end
      print "\nIs the information correct?(Y/n):"
      unless STDIN.gets.chomp.downcase.start_with? "n"
	puts "Master node info changed"
	should_gen = true
	break
      end
    end
  end

  if workers.length != 0
    puts "\nExisting worker nodes info:"
    workers.each do |w|
      puts ""
      w.each do |key, value|
	puts " * #{key} = #{value}"
      end
    end
    print "\nAre you going to change the list of workers(y/N):"
    if STDIN.gets.chomp.downcase.start_with? "y"
      workers = []
    else
      puts "Worker nodes info not changed"
    end
  end
    
  if workers.length == 0
    loop do
      print "Add new worker info?(Y/n):"
      if STDIN.gets.chomp.downcase.start_with? "n"
	break
      end
      w = {}
      loop do
	print "Intranet IP: "
	w["intranet_ip"] = STDIN.gets.chomp
	puts ""
	w.each do |key, value|
	    puts " * #{key} = #{value}"
	end
	print "\nIs the information correct?(Y/n):"
	break unless STDIN.gets.chomp.downcase.start_with? "n"
      end
      workers << w
      should_gen = true
      puts "Worker node list updated"
    end
  end
 
  if should_gen
    node_data = {}
    node_data["master"] = master
    node_data["workers"] = workers
    File.open(NODE_YAML, "w") do |f|
      yaml_str = YAML::dump node_data
      file_content = <<NODE_YAML_HEAD
# list of nodes
# !!!DO NOT MODIFY!!!
# this file is generated according to administrator's configuration
# comments start with '#'

# hostname role
#{yaml_str}
NODE_YAML_HEAD
      f.write file_content
      puts "YAML data:"
      puts yaml_str
      puts "Updated 'node.yaml' file"
    end
  else
    puts "'node.yaml' not modified"
  end
  
end
