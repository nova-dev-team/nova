# This is the Rakefile for managing my Workspace
#
# Santa Zhang (santa1987@gmail.com)

require 'fileutils'

def my_exec cmds
  cmds.each_line do |cmd|
    cmd = cmd.strip
    puts "[cmd] #{cmd}"
    system cmd
  end
end


# randomly generate a token with alphabets, like 'acd', 'zfc', etc.
def random_token length = 5
  token = ""
  alphabets = "abcdefghijklmnopqrstuvwxyz"
  1.upto(length) do |i|
    idx = rand(alphabets.length)
    token += alphabets[idx..idx]
  end
  return token
end

def nth_line text, nth
  counter = 0
  text.each_line do |line|
    line = line.chomp
    counter += 1
    return line if counter == nth
  end
  return nil
end

desc "Make distribution tar package"
task :dist do
  token = random_token

  project_name = File.basename(File.expand_path(File.dirname(__FILE__)))

  source_root = File.dirname __FILE__
  
  # check if clean (git status, line 2 not start with '#')
  if nth_line(`git status`, 4) != nil and nth_line(`git status`, 4).start_with? '#'
    puts "*** not a clean workspace, please git commit & git push first!"
    exit
  end
  
  # ok, workspace is clean, now determine tar_fn post fix
  current_commit_hash = nth_line(`git show`, 1).split[1]
  puts current_commit_hash
  
  tar_postfix = "snapshot-#{current_commit_hash[0..6]}"
  
  `git tag`.each_line do |line|
    tag = line.strip
    tag_commit_hash = nth_line(`git show #{tag}`, 1).split[1]
    if tag_commit_hash == current_commit_hash
      tar_postfix = tag
      break
    end
  end
  
  if tar_postfix != ""
    tar_main_fn = "#{project_name}-#{tar_postfix}"
  else
    tar_main_fn = project_name
  end
  tar_fn = "#{source_root}/#{tar_main_fn}.tar.gz"

  # git clone to a temp dir in '/tmp'
  clone_folder_parent = "/tmp/#{project_name}-make-tar.#{token}"
  clone_folder = "#{clone_folder_parent}/#{tar_main_fn}"

  my_exec <<CMD
git clone #{source_root} #{clone_folder}
rm -Rf #{clone_folder}/.git
#{
  if defined? IGNORE_IN_TAR
    (IGNORE_IN_TAR.collect {|e| "rm -Rf #{clone_folder}/#{e}"}).join "\n"
  end
}
rm -f #{tar_fn}
cd #{clone_folder}/.. && tar pczf #{tar_fn} #{tar_main_fn}
rm -Rf #{clone_folder_parent}
CMD
  puts "Created #{tar_main_fn}.tar.gz in source root folder!"
end

task :default => :dist

