def windows?
  !(RUBY_PLATFORM =~ /win32/).nil?
end

CEIL_WIN = File.dirname(__FILE__) + 'ceil_win.rb'
CEIL_LINUX = File.dirname(__FILE__) + 'ceil.rb'

if windows?
  require 'ceil_win'
#	system CEIL_WIN	
else
  require 'ceil'
#  system CEIL_LINUX
end

