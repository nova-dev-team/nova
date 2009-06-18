#! /usr/bin/env ruby

require "dbus"
require 'optparse'
require 'ostruct'
require 'pp'


$VERSION = 'version 2, support dbus stroage engine'


class Optparse

  #
  # Return a structure describing the options.
  #
  def self.parse(args)
    options = OpenStruct.new
    options.verbose = false
    options.bus = 'system'
    options.listen = false

    opts = OptionParser.new do |opts|
      opts.banner = "Usage: sec.rb [options]"

      opts.separator ""
      opts.separator "Specific options:"

      opts.on("-a", "--action [s|p|c|start|stop|check]", "动作") do |action|
          options.action = action
      end

      opts.on("-b", "--bus [system|session]", "指定dbus(缺省system)") do |bus|
          options.bus = bus
      end

      opts.on("-l", "--listen", "listen signal") do
          options.listen = true
      end

      # Boolean switch.
      opts.on("-v", "--[no-]verbose", "Run verbosely") do |v|
        options.verbose = v
      end

      opts.separator ""
      opts.separator "Common options:"

      # No argument, shows at tail.  This will print an options summary.
      # Try it and see!
      opts.on_tail("-h", "--help", "Show this message") do
        puts opts
        exit
      end

      # Another typical switch to print the version.
      opts.on_tail("--version", "Show version") do
        puts $VERSION
        exit
      end
    end

    opts.parse!(args)
    options
  end  # parse()

end


options = Optparse.parse(ARGV)

case options.bus
when 'system' then bus = DBus::SystemBus.instance
when 'session' then bus = DBus::SessionBus.instance
else
  puts '未指定dbus'
  exit
end

ruby_srv = bus.service("cn.org.btc.UpdateEngine")

# Get the object from this service
player = ruby_srv.object("/cn/org/btc/UpdateEngine")
player.default_iface = "cn.org.btc.UpdateEngine"

intr = player.introspect
if options.verbose
  pp intr
end

if options.listen
  player.on_signal('ue_started') {
    puts "on start"
  }
  player.on_signal('ue_stoped') {
    puts "on stoped #{uuid}"
  }
  player.on_signal('download_progress') { |uuid, progress|
    puts "on download progress #{uuid}, #{progress}"
  }

  main = DBus::Main.new
  main << bus
  main.run
end

case options.action
when 'restart', 'start', 's'
  player.start
when 'stop', 'shut', 'p', 'shutdown'
  player.stop
when 'stat', 'status', 'check', 'c'
  player.check
else
  puts '--action is 必需的'
end

