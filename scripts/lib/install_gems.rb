#!/usr/bin/ruby

require File.dirname(__FILE__) + '/utils.rb'

GEMS_FOLDER = File.dirname(__FILE__) + '/../data/installer/gems'

$installed_gems = {}

IO.popen("gem list") do |pipe|
  lines = pipe.readlines
  lines.each do |line|
    splt = line.split /\(|\)| |\t|,|\n|\r/
    gem_name = splt[0]
    version_list = []
    
    splt[1..-1].each do |ver|
      next if ver.length == 0
      version_list << ver
    end
    $installed_gems[gem_name] = version_list
  end
end

def already_installed? fname
  idx = fname.rindex '-'
  gem_name = fname[0..(idx - 1)]
  ver = fname[(idx + 1)..-5]
  $installed_gems[gem_name] and $installed_gems[gem_name].include? ver
end

def install_gem fname
  sys_exec "cd #{GEMS_FOLDER} && gem install --no-ri --no-rdoc -l #{fname}"
end

Dir.entries(GEMS_FOLDER).each do |f|
  if f.end_with? ".gem"
    if already_installed? f
      puts "gem package '#{f}' is already installed, skip installation..."
    else
      install_gem f
    end
  end
end

