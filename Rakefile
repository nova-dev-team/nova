
############################################################################
####  Settings section
############################################################################


# these are const values required for building KFS module
KFS_DIR = "kfs-0.3"
KFS_JAVA_INCLUDE_PATH = "/usr/lib/jvm/java-sun-6/include"
KFS_JAVA_INCLUDE_PATH2 = "/usr/lib/jvm/java-sun-6/include/linux"

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


############################################################################
####  Task definition section
############################################################################

task :default => :about

desc "Show information about the installer"
task :about do
puts <<ABOUT
Hello, this is the Nova platform installer!
TODO add more detail info on this Rakefile
ABOUT
end

namespace :clean do

  desc "Clean the build files for KFS"
  task :kfs do
    raise RuntimeError, "*** Tool 'make' was not installed!" unless installed? "make"
    puts "Cleaning KFS build files..."
    
    system "cd #{KFS_DIR} && make clean"
    
    puts "Done cleaning KFS build files"
  end
  
end

namespace :compile do
  
  desc "Compile KFS for storage module"
  task :kfs do
    
    kfs_files = [
      "chunk/chunkserver",
      "chunk/chunkscrubber",
      "emulator/rebalanceexecutor",
      "emulator/rebalanceplanner",
      "emulator/replicachecker",
      "emulator/rereplicator",
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
    
    should_build = false
    
    kfs_files.each do |file|
      unless File.exists? "#{KFS_DIR}/src/cc/#{file}"
        should_build = true
        break
      end
    end
    
    if should_build
    
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
    else
      puts "KFS module is already compiled"
    end

  end
end


namespace :deploy do

  desc "Deploy the 'core' component"  
  task :core do
    puts "TODO deploy core"
 end

  desc "Deploy the 'pnode' component"
  task :pnode do
    puts "TODO deploy pnode"
  end

  namespace :storage do
    desc "Deploy this computer as a KFS meta server"
    task :kfsmeta => "compile:kfs" do
      puts "TODO deploy kfsmeta"
    end
    
    desc "Deploy this computer as a KFS chunk server"
    task :kfschunk => "compile:kfs" do
      puts "TODO deploy kfschunk"
    end
  end
end

