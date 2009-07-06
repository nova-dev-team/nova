task :default => :about

desc "Show information about the installer"
task :about do
puts <<ABOUT
Hello, this is the Nova platform installer!
ABOUT
end

namespace :deploy do

  desc "Deploy the 'core' component"  
  task :core do
    puts "TODO"
  end

  desc "Deploy the 'pnode' component"
  task :pnode do
    puts "TODO"
  end

  namespace :storage do

    desc "Deploy this computer as a KFS storage server"
    task :kfs do
      `sdf`
      puts "TODO"
    end

  end

end

