require File.dirname(__FILE__) + "/../lib/utils.rb"

NODE_LIST = File.dirname(__FILE__) + "/node.list"

def gen_node_list
  master = nil
  workers = []
  should_gen = false # should we really generate the node_list

  if File.exist? NODE_LIST
    puts "Existing 'node.list' detected"
    all_nodes do |host, role|
      master = host if role == "master"
      workers << host if role == "worker"
    end
  end

  puts "Configuring 'node.list' file"

  if master != nil:
    print "Master(#{master}):"
    input = STDIN.gets.chomp
    if input != ""
      master = input
      should_gen = true
    else
      puts "Master node not changed"
    end
  else
    loop do
      print "Master:"
      input = STDIN.gets.chomp
      if input == ""
        puts "You must give the master node!"
      else
        master = input
        should_gen = true
        break
      end
    end
  end

  if workers.length != 0
    puts "\nCurrent workers:"
    workers.each do |w|
      puts w
    end
    print "\nAre you going to change the list of workers(y/N):"
    if STDIN.gets.chomp.downcase.start_with? "y"
      workers = []
    end
  end
    
  if workers.length == 0
    puts "Input worker nodes list, one hostname per line, terminated by an empty line:"
    loop do
      input = STDIN.gets.chomp
      if input == ""
        break
      end
      should_gen = true
      workers << input
    end
  end
 
  if should_gen
    File.open(NODE_LIST, "w") do |f|
      node_list_str = ""
      node_list_str += "#{master} master\n"
      workers.each do |w|
        node_list_str += "#{w} worker\n"
      end
      f.write <<EOF_NODE_LIST
# list of nodes
# !!!DO NOT MODIFY!!!
# this file is generated according to administrator's configuration
# comments start with '#'

# hostname role
#{node_list_str}
EOF_NODE_LIST
      puts "Updated 'node.list' file"
    end
  else
    puts "'node.list' not modified"
  end
  
end

