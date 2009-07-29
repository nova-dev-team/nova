require 'pp'
require 'xmlsimple'

############################################################################
####  Helper function section
############################################################################

# a helper function to detect whether a tool was installed
def installed? tool
  # use 'which' command to check if a tool exists
  print "Checking if has '#{tool}'... "
  pipe = IO.popen("which #{tool}", "r")
  
  begin
    which_result = pipe.readline.chomp
  rescue
    which_result = nil
  ensure
    pipe.close
  end
  
  if which_result and File.exists? which_result
    puts "YES"
    true
  else
    puts "NO"
    false
  end
end




def xml_get
  xml = XmlSimple.xml_in('nova_deploy.xml')
  xml
end


def xml_core
  xml = xml_get
  core_xml = {}
  xml["core"][0].each { |key, val| core_xml[key] = val[0] }
  core_xml
end



def xml_pnodes
  xml = xml_get
  pnodes_xml = []
  xml["pnodes"][0].each do |key, value|
    value.each do |val|
      item = {}
      item["name"] = val["name"]
      item["rundir"] = val["rundir"][0]
      item["host"] = val["host"][0]
      item["port"] = val["port"][0]
      pnodes_xml << item
    end
  end
  pnodes_xml
end



def xml_storages
  xml = xml_get
  xml["storages"][0]["storage"]
end



def xml_settings
  xml = xml_get
  settings = {}
  xml["settings"][0]["setting"].each { |item| settings[item["name"]] = item["val"] }
  settings
end


############################################################################
####  Task definition section
############################################################################

task :default => :about

desc "Show information about the installer"
task :about do
puts <<ABOUT
Hello, this is the Nova platform installer!
Type the following command and drink a cup of coffee! :D
rake start
TODO add more detail info on this Rakefile
ABOUT
end

desc "Show help information"
task :help do
puts <<HELP
TODO HELP info
HELP
end

desc "Deploy the Nova system to a cluster"
task :deploy => ["deploy:core", "deploy:pnode", "deploy:storage"] do
end

namespace :deploy do

  desc "Deploy the 'core' component"  
  task :core do
    xml = xml_core
    puts "Deploying 'core' component to #{xml["host"]}:#{xml["rundir"]}"
    
    `mkdir tmp -p`
    `cp -r core tmp/`
    system "cd tmp/core && rake db:migrate:reset && rake db:fixtures:load"
    `rm tmp/core/log/*`
    `rm tmp/core/tmp/* -R`
    
    f = File.new "tmp/core/start", "w"
    f.write <<CORE_START
ruby script/server -b #{xml["host"]} -p #{xml["port"]} -d
CORE_START
    f.close

    f = File.new "tmp/core/stop", "w"
    f.write <<CORE_STOP
killall ruby
CORE_STOP
    f.close
    
    f = File.new "tmp/core/uninstall", "w"
    f.write <<CORE_UNINSTALL
killall ruby
cd ..
rm core -R
CORE_UNINSTALL
    f.close
    
    system "scp -C -r tmp/core #{xml["host"]}:#{xml["rundir"]}"
    `rm tmp -R`
  end



  desc "Deploy all 'pnode' components"
  task :pnode do
    xml_pnodes.each do |xml|
      puts "Deploying 'pnode' component to #{xml["host"]}:#{xml["rundir"]}"
      
      `mkdir tmp/#{xml["name"]} -p`

      f = File.new "tmp/#{xml["name"]}/start", "w"
      f.write <<PNODE_START
(ruby server.rb -s mongrel -p #{xml["port"]})&
PNODE_START
      f.close

      f = File.new "tmp/#{xml["name"]}/stop", "w"
      f.write <<PNODE_STOP
killall ruby
PNODE_STOP
      f.close
      
      f = File.new "tmp/#{xml["name"]}/uninstall", "w"
      f.write <<PNODE_UNINSTALL
killall ruby
cd ..
rm #{xml["name"]} -R
PNODE_UNINSTALL
      f.close
    
      `cp -r pnode/* tmp/#{xml["name"]}`
      
      system "scp -r tmp/#{xml["name"]} #{xml["host"]}:#{xml["rundir"]}"
      `rm tmp -R`
    end
  end



  desc "Deploy all 'storage' components"
  task :storage do
   
  end
end

desc "Start the Nova system"
task :start => ["start:core", "start:pnode", "start:storage"] do
end

namespace :start do
  
  desc "Start the 'core' component"
  task :core do
    puts "Starting the 'core' component. When started, you might need to press ENTER."
    xml = xml_core
    system "ssh #{xml["host"]} 'cd #{xml["rundir"]}/core && bash start && exit'"
  end
  
  desc "Start all 'pnode' components"
  task :pnode do
    xml_pnodes.each do |xml|
      puts "Starting 'pnode' component on #{xml["host"]}:#{xml["port"]}"
      puts "Please wait a few seconds and press ENTER."
      system "ssh #{xml["host"]} 'cd #{xml["rundir"]}/#{xml["name"]} && bash start && exit'"
    end
  end
  
  desc "Start all 'storage' components"
  task :storage do

  end

end

desc "Stop the Nova system"
task :stop => ["stop:core", "stop:pnode", "stop:storage"] do
end

namespace :stop do
  
  desc "Stop the 'core' component"
  task :core do
    puts "Stopping the 'core' component"
    xml = xml_core
    system "ssh #{xml["host"]} 'cd #{xml["rundir"]}/core && bash stop && exit'"
  end

  desc "Stop all 'pnode' components"
  task :pnode do
    xml_pnodes.each do |xml|
      puts "Stopping 'pnode' component on #{xml["host"]}:#{xml["port"]}"
      system "ssh #{xml["host"]} 'cd #{xml["rundir"]}/#{xml["name"]} && bash stop && exit'"
    end
  end
  
  desc "Stop all 'storage' components"
  task :storage do
   
  end
  
end

desc "Uninstall the Nova system"
task :uninstall => ["uninstall:pnode", "uninstall:core", "uninstall:storage"] do
end


namespace :uninstall do

  desc "Uninstall the 'core' component"
  task :core do
    puts "Stopping the 'core' component"
    xml = xml_core
    system "ssh #{xml["host"]} 'cd #{xml["rundir"]}/core && bash uninstall && exit'"
  end

  desc "Uninstall all 'pnode' components"
  task :pnode do
    xml_pnodes.each do |xml|
      puts "Stopping 'pnode' component on #{xml["host"]}:#{xml["port"]}"
      system "ssh #{xml["host"]} 'cd #{xml["rundir"]}/#{xml["name"]} && bash uninstall && exit'"
    end
  end
  
  desc "Uninstall all 'storage' components"
  task :storage do
    
  end

end

