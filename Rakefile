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



def xml_kfs
  
  def size_unit_conv str
    val = str.to_f
    if str["G"]
      val *= 1000 * 1000 * 1000
    elsif str["M"]
      val *= 1000 * 1000
    elsif str["K"]
      val *= 1000
    end
    val.to_i
  end

  xml = []
  xml_storages.each do |x|
    if x["type"] == "kfs"
      x["metaserver"] = x["metaserver"][0]
      x["chunkserver"].each do |chunk|
        chunk["rundir"] = chunk["rundir"][0]
        chunk["clientport"] = chunk["clientport"][0]
        chunk["space"] = size_unit_conv chunk["space"][0]
        chunk["host"] = chunk["host"][0]
        chunk["internalport"] = chunk["internalport"][0]
      end
      xml << x
    end
  end
  xml
end



def need_kfs?
  xml = xml_storages
  xml.each { |x| return true if x["type"] == "kfs" }
  false
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


desc "Clean all build files"
task :clean => "clean:kfs" do
end


namespace :clean do

  desc "Clean the build files for KFS"
  task :kfs do
    if need_kfs?
    
      xml = xml_settings
      
      KFS_DIR = xml["KFS_DIR"]
      
      raise RuntimeError, "*** Tool 'make' was not installed!" unless installed? "make"
      puts "Cleaning KFS build files..."
      
      system "cd #{KFS_DIR} && make clean"
      puts "Done cleaning KFS build files"
      
    else
      puts "KFS is not required according to your 'nova_deploy.xml'"
    end
  end
  
end


desc "Compile all needed binaries"
task :compile => "compile:kfs" do
end


namespace :compile do
  
  desc "Compile KFS for storage module"
  task :kfs do
  
    if need_kfs?
    
      xml = xml_settings
      
      KFS_DIR = xml["KFS_DIR"]
      KFS_JAVA_INCLUDE_PATH = xml["KFS_JAVA_INCLUDE_PATH"]
      KFS_JAVA_INCLUDE_PATH2 = xml["KFS_JAVA_INCLUDE_PATH2"]
      
      kfs_files = [
        "chunk/chunkserver",
        "chunk/chunkscrubber",
        "emulator/rebalanceexecutor",
        "emulator/rebalanceplanner",
        "emulator/replicachecker",
        "emulator/rereplicator",
        "fuse/kfs_fuse",
        "meta/logcompactor",
        "meta/metaserver",
        "tests/KfsDataGen",
        "tests/KfsDirFileTester",
        "tests/KfsDirScanTest",
        "tests/KfsPerfReader",
        "tests/KfsPerfWriter",
        "tests/KfsReader",
        "tests/KfsRW",
        "tests/KfsSeekWrite",
        "tests/KfsTrunc",
        "tests/KfsWriter",
        "tests/mkfstree",
        "tools/cpfromkfs",
        "tools/cptokfs",
        "tools/kfsdataverify",
        "tools/kfsfileenum",
        "tools/kfsping",
        "tools/kfsput",
        "tools/kfsretire",
        "tools/kfsshell",
        "tools/kfsstats",
        "tools/kfstoggleworm"
      ]
      
      already_built = true
      
      kfs_files.each do |file|
        unless File.exists? "#{KFS_DIR}/src/cc/#{file}"
          already_built = false
          break
        end
      end
      
      if already_built
        puts "KFS module is already compiled"      
        
      else
        puts "Compiling KFS for storage module"
        puts "Checking installed tools"
        
        required_tools = ['cmake', 'g++', 'make']
        required_tools.each do |tool|
          unless installed? tool
            raise RuntimeError, "*** Tool '#{tool}' was not installed!"
          end
        end
        
        system "cmake -DJAVA_INCLUDE_PATH=#{KFS_JAVA_INCLUDE_PATH} -DJAVA_INCLUDE_PATH2=#{KFS_JAVA_INCLUDE_PATH2} #{KFS_DIR}"
        system "cd #{KFS_DIR} && make"
        system "mkdir #{KFS_DIR}/bin -p"
      end

    else
      puts "KFS is not required according to your 'nova_deploy.xml'"      
    end
    
  end
end


desc "Deploy the Nova system to a cluster"
task :deploy => ["deploy:core", "deploy:pnode", "deploy:kfs"] do
end

namespace :deploy do

  desc "Deploy the 'core' component"  
  task :core => "compile:kfs" do
    xml = xml_core
    puts "Deploying 'core' component to #{xml["host"]}:#{xml["rundir"]}"
    
    `mkdir tmp -p`
    `cp -r core tmp/`
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
  task :pnode => "compile:kfs" do
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
  task :storage => "compile" do
    
    if need_kfs?
      puts "Deploying the KFS storage module"
      KFS_DIR = (xml_settings)["KFS_DIR"]
      
      `mkdir tmp -p`
      
      xml_kfs.each do |xml|
        puts "Deploying meta server '#{xml["metaserver"]["name"]}'..."
        
        `mkdir tmp/#{xml["metaserver"]["name"]}/kfslog -p`
        `mkdir tmp/#{xml["metaserver"]["name"]}/kfscp -p`
        `cp #{KFS_DIR}/src/cc/meta/metaserver tmp/#{xml["metaserver"]["name"]}/`
        
        f = File.new "tmp/#{xml["metaserver"]["name"]}/kfs_config_#{xml["metaserver"]["name"]}", "w"
        f.write <<META_CONF
metaServer.clientPort = #{xml["metaserver"]["clientport"]}
metaServer.chunkServerPort = #{xml["metaserver"]["internalport"]}
metaServer.logDir = ./kfslog
metaServer.cpDir = .kfscp
META_CONF
        f.close

        f = File.new "tmp/#{xml["metaserver"]["name"]}/start", "w"
        f.write <<META_START
./metaserver kfs_config_#{xml["metaserver"]["name"]}
META_START
        f.close

        f = File.new "tmp/#{xml["metaserver"]["name"]}/stop", "w"
        f.write <<META_STOP
killall metaserver
META_STOP
        f.close

        f = File.new "tmp/#{xml["metaserver"]["name"]}/uninstall", "w"
        f.write <<META_UNINSTALL
killall metaserver
cd ..
rm #{xml["metaserver"]["name"]} -R
META_UNINSTALL
        f.close

        
        system "scp -r tmp/#{xml["metaserver"]["name"]} #{xml["metaserver"]["host"]}:#{xml["metaserver"]["rundir"]}"
        `rm tmp/#{xml["metaserver"]["name"]} -R`
        
        xml["chunkserver"].each do |chunk|
          puts "Deploying chunk server '#{chunk["name"]}'..."
          
          `mkdir tmp/#{chunk["name"]}/chunks -p`
          `mkdir tmp/#{chunk["name"]}/logs -p`
          `cp #{KFS_DIR}/src/cc/chunk/chunkserver tmp/#{chunk["name"]}/`
          
          f = File.new "tmp/#{chunk["name"]}/kfs_config_#{chunk["name"]}", "w"
          f.write <<CHUNK_CONF
chunkServer.metaServer.hostname = #{xml["metaserver"]["host"]}
chunkServer.metaServer.port = #{xml["metaserver"]["internalport"]}
chunkServer.clientPort = #{chunk["clientport"]}
chunkServer.chunkDir = ./chunks
chunkServer.logDir = ./logs
chunkServer.totalSpace = #{chunk["space"]}
CHUNK_CONF
          f.close

        f = File.new "tmp/#{chunk["name"]}/start", "w"
        f.write <<CHUNK_START
./chunkserver kfs_config_#{chunk["name"]}
CHUNK_START
        f.close

        f = File.new "tmp/#{chunk["name"]}/stop", "w"
        f.write <<CHUNK_STOP
killall chunkserver
CHUNK_STOP
        f.close

        f = File.new "tmp/#{chunk["name"]}/uninstall", "w"
        f.write <<CHUNK_UNINSTALL
killall chunkserver
cd ..
rm #{chunk["name"]} -R
CHUNK_UNINSTALL
        f.close
                    
          system "scp -r tmp/#{chunk["name"]} #{chunk["host"]}:#{chunk["rundir"]}"
          `rm tmp/#{chunk["name"]} -R`
        end
      end
      
      `rm tmp -R`
      
    else
      puts "KFS is not required according to your 'nova_deploy.xml'"
    end
    
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
    xml_kfs.each do |xml|
      puts "Starting meta server '#{xml["metaserver"]["name"]}' on #{xml["metaserver"]["host"]}..."
      puts "Please wait a few seconds and press ENTER."
      system "ssh #{xml["metaserver"]["host"]} 'cd #{xml["metaserver"]["rundir"]}/#{xml["metaserver"]["name"]} && (bash start)& exit'"
      
      xml["chunkserver"].each do |chunk|
        puts "Starting chunk server '#{chunk["name"]}'..."
        puts "Please wait a few seconds and press ENTER."
        system "ssh #{chunk["host"]} 'cd #{chunk["rundir"]}/#{chunk["name"]} && (bash start)& exit'"
      end
    end
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
    xml_kfs.each do |xml|
      puts "Stopping KFS meta server '#{xml["metaserver"]["name"]}' on #{xml["metaserver"]["host"]}..."
      system "ssh #{xml["metaserver"]["host"]} 'cd #{xml["metaserver"]["rundir"]}/#{xml["metaserver"]["name"]} && bash stop && exit'"
      
      xml["chunkserver"].each do |chunk|
        puts "Stopping KFS chunk server '#{chunk["name"]}'..."
        system "ssh #{chunk["host"]} 'cd #{chunk["rundir"]}/#{chunk["name"]} && bash stop && exit'"
      end
    end
  end
  
end

desc "Uninstall the Nova system"
task :uninstall => ["uninstall:pnode", "uninstall:core", "uninstall:kfs"] do
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
    xml_kfs.each do |xml|
      puts "Uninstalling KFS meta server '#{xml["metaserver"]["name"]}' on #{xml["metaserver"]["host"]}..."
      system "ssh #{xml["metaserver"]["host"]} 'cd #{xml["metaserver"]["rundir"]}/#{xml["metaserver"]["name"]} && bash uninstall && exit'"
      
      xml["chunkserver"].each do |chunk|
        puts "Uninstalling KFS chunk server '#{chunk["name"]}'..."
        system "ssh #{chunk["host"]} 'cd #{chunk["rundir"]}/#{chunk["name"]} && bash uninstall && exit'"
      end
    end  
  end
    
end

