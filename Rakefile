
############################################################################
####  Settings section
############################################################################


# these are const values required for building KFS module



############################################################################
####  Helper class section
############################################################################

#
# ini.rb - read and write ini files
#
# Copyright (C) 2007 Jeena Paradies
# License: GPL
# Author: Jeena Paradies (info@jeenaparadies.net)
#
# == Overview
#
# This file provides a read-wite handling for ini files.
# The data of a ini file is represented by a object which
# is populated with strings.
 
class Ini
  
  # Class with methods to read from and write into ini files.
  #
  # A ini file is a text file in a specific format,
  # it may include several fields which are sparated by
  # field headlines which are enclosured by "[]".
  # Each field may include several key-value pairs.
  #
  # Each key-value pair is represented by one line and
  # the value is sparated from the key by a "=".
  #
  # == Examples
  #
  # === Example ini file
  #
  #   # this is the first comment which will be saved in the comment attribute
  #   mail=info@example.com
  #   domain=example.com # this is a comment which will not be saved
  #   [database]
  #   db=example
  #   user=john
  #   passwd=very-secure
  #   host=localhost
  #   # this is another comment
  #   [filepaths]
  #   tmp=/tmp/example
  #   lib=/home/john/projects/example/lib
  #   htdocs=/home/john/projects/example/htdocs
  #   [ texts ]
  #   wellcome=Wellcome on my new website!
  #   Website description = This is only a example. # and another comment
  #
  # === Example object
  #
  #   A Ini#comment stores:
  #   "this is the first comment which will be saved in the comment attribute"
  #
  #   A Ini object stores:
  #
  #   {
  #    "mail" => "info@example.com",
  #    "domain" => "example.com",
  #    "database" => {
  #     "db" => "example",
  #     "user" => "john",
  #     "passwd" => "very-secure",
  #     "host" => "localhost"
  #    },
  #    "filepaths" => {
  #     "tmp" => "/tmp/example",
  #     "lib" => "/home/john/projects/example/lib",
  #     "htdocs" => "/home/john/projects/example/htdocs"
  #    }
  #    "texts" => {
  #     "wellcome" => "Wellcome on my new website!",
  #     "Website description" => "This is only a example."
  #    }
  #   }
  #
  # As you can see this module gets rid of all comments, linebreaks
  # and unnecessary spaces at the beginning and the end of each
  # field headline, key or value.
  #
  # === Using the object
  #
  # Using the object is stright forward:
  #
  #   ini = Ini.new("path/settings.ini")
  #   ini["mail"] = "info@example.com"
  #   ini["filepaths"] = { "tmp" => "/tmp/example" }
  #   ini.comment = "This is\na comment"
  #   puts ini["filepaths"]["tmp"]
  #   # => /tmp/example
  #   ini.write()
  # 
  
  #
  # :inihash is a hash which holds all ini data
  # :comment is a string which holds the comments on the top of the file
  #
  attr_accessor :inihash, :comment
 
  #
  # Creating a new Ini object
  #
  # +path+ is a path to the ini file
  # +load+ if nil restores the data if possible
  #        if true restores the data, if not possible raises an error
  #        if false does not resotre the data
  #
  def initialize(path, load=nil)
    @path = path
    @inihash = {}
    
    if load or ( load.nil? and FileTest.readable_real? @path )
      restore()
    end
  end
  
  #
  # Retrive the ini data for the key +key+
  #
  def [](key)
    @inihash[key]
  end
  
  #
  # Set the ini data for the key +key+
  #
  def []=(key, value)
    raise TypeError, "String expected" unless key.is_a? String
    raise TypeError, "String or Hash expected" unless value.is_a? String or value.is_a? Hash
    
    @inihash[key] = value
  end
  
  #
  # Restores the data from file into the object
  #
  def restore()
    @inihash = Ini.read_from_file(@path)
    @comment = Ini.read_comment_from_file(@path)
  end
  
  #
  # Store data from the object in the file
  #
  def update()
    Ini.write_to_file(@path, @inihash, @comment)
  end
 
  #
  # Reading data from file
  #
  # +path+ is a path to the ini file
  #
  # returns a hash which represents the data from the file
  #
  def Ini.read_from_file(path)
        
    inihash = {}
    headline = nil
    
    IO.foreach(path) do |line|
 
      line = line.strip.split(/#/)[0]

      # bug fix by santa
      next unless line
      
      # read it only if the line doesn't begin with a "=" and is long enough
      unless line.length < 2 and line[0,1] == "="
        
        # it's a headline if the line begins with a "[" and ends with a "]"
        if line[0,1] == "[" and line[line.length - 1, line.length] == "]"
          
          # get rid of the [] and unnecessary spaces
          headline = line[1, line.length - 2 ].strip
          inihash[headline] = {}
        else
        
          key, value = line.split(/=/, 2)
          
          key = key.strip unless key.nil?
          value = value.strip unless value.nil?
          
          unless headline.nil?
            inihash[headline][key] = value
          else
            inihash[key] = value unless key.nil?
          end
        end        
      end
    end
    
    inihash
  end
  
  #
  # Reading comments from file
  #
  # +path+ is a path to the ini file
  #
  # Returns a string with comments from the beginning of the
  # ini file.
  #
  def Ini.read_comment_from_file(path)
    comment = ""
    
    IO.foreach(path) do |line|
      line.strip!
      break unless line[0,1] == "#" or line == ""
      next unless line and line.length > 0
      comment << "#{line[1, line.length ].strip}\n"
    end
    
    comment
  end
  
  #
  # Writing a ini hash into a file
  #
  # +path+ is a path to the ini file
  # +inihash+ is a hash representing the ini File. Default is a empty hash.
  # +comment+ is a string with comments which appear on the
  #           top of the file. Each line will get a "#" before.
  #           Default is no comment.
  #
  def Ini.write_to_file(path, inihash={}, comment=nil)
    raise TypeError, "String expected" unless comment.is_a? String or comment.nil?
    
    raise TypeError, "Hash expected" unless inihash.is_a? Hash
    File.open(path, "w") { |file|
      
      unless comment.nil?
        comment.each do |line|
          file << "# #{line}"
        end
      end
      
      file << Ini.to_s(inihash)
    }
  end
  
  #
  # Turn a hash (up to 2 levels deepness) into a ini string
  #
  # +inihash+ is a hash representing the ini File. Default is a empty hash.
  #
  # Returns a string in the ini file format.
  #
  def Ini.to_s(inihash={})
    str = ""
    
    inihash.each do |key, value|
 
      if value.is_a? Hash
        str << "[#{key.to_s}]\n"
        
        value.each do |under_key, under_value|
          str << "#{under_key.to_s}=#{under_value.to_s unless under_value.nil?}\n"
        end
 
      else
        str << "#{key.to_s}=#{value.to_s unless value2.nil?}\n"
      end
    end
    
    str
  end
  
end




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
    
    inihash = (Ini.new "kfs_deploy.ini").inihash
    KFS_DIR = inihash["KFS_DIR"]
    
    system "cd #{KFS_DIR} && make clean"
    
    puts "Done cleaning KFS build files"
  end
  
end

namespace :compile do
  
  desc "Compile KFS for storage module"
  task :kfs do
    inihash = (Ini.new "kfs_deploy.ini").inihash
    
    KFS_DIR = inihash["KFS_DIR"]
    KFS_JAVA_INCLUDE_PATH = inihash["KFS_JAVA_INCLUDE_PATH"]
    KFS_JAVA_INCLUDE_PATH2 = inihash["KFS_JAVA_INCLUDE_PATH2"]
    
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

  desc "Deploy the KFS storage module"
  task :kfs => "compile:kfs" do
    puts "Deploying the KFS storage module"
    inihash = (Ini.new "kfs_deploy.ini").inihash

    meta_server_node = nil
    meta_server_port = nil

    # first deploy the meta server
    inihash.each do |key, val|
      if val["type"] == "metaserver"
        puts "Deploying #{key}..."
        
        f = File.new "kfs_config_#{key}", "w"
        
        KFSLOGDIR = "./kfslog"
        KFSCPDIR = "./kfscp"
        
        f.write <<META_CONF
metaServer.clientPort = #{val["clientport"]}
metaServer.chunkServerPort = #{val["baseport"]}
metaServer.logDir = #{KFSLOGDIR}
metaServer.cpDir = #{KFSCPDIR}
META_CONF
        f.close

        meta_server_node = val["node"]
        meta_server_port = val["baseport"]
        
        puts "Copying files to node #{val["node"]}"
        puts "Your password might be required"
        system "scp #{KFS_DIR}/src/cc/meta/metaserver kfs_config_#{key} #{val["node"]}:#{val["rundir"]} \
                && rm kfs_config_#{key}"

        puts "Your password might be required again, after that, keep pressing ENTER :D"
        system "ssh #{val["node"]} 'cd #{val["rundir"]} && mkdir #{KFSLOGDIR} -p \
                && mkdir #{KFSCPDIR} -p && (./metaserver kfs_config_#{key})& exit'"
      end
    end

    
    def size_unit_conv str
      base = str.to_i
      if str["G"]
        base *= 1000 * 1000 * 1000
      elsif str["M"]
        base *= 1000 * 1000
      elsif str["K"]
        base *= 1000
      end
      base
    end

    inihash.each do |key, val|
      if val["type"] == "chunkserver"
        puts "Deploying #{key}..."

        f = File.new "kfs_config_#{key}", "w"
        
        f.write <<CHUNK_CONF
chunkServer.metaServer.hostname = #{meta_server_node}
chunkServer.metaServer.port = #{meta_server_port}
chunkServer.clientPort = #{val["clientport"]}
chunkServer.chunkDir = ./chunks
chunkServer.logDir = ./logs
chunkServer.totalSpace = #{size_unit_conv val["space"]}
CHUNK_CONF
        f.close

        puts "Copying files to node #{val["node"]}"
        puts "Your password might be required"
        system "scp #{KFS_DIR}/src/cc/chunk/chunkserver kfs_config_#{key} #{val["node"]}:#{val["rundir"]} \
                && rm kfs_config_#{key}"
        
        puts "Your password might be required again, after that, keep pressing ENTER :D"
        system "ssh #{val["node"]} 'cd #{val["rundir"]} && mkdir chunks -p && (./chunkserver kfs_config_#{key})& exit'"
      end
    end
  end

end

