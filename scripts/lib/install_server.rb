#!/usr/bin/ruby

require 'socket'
require 'thread'

class InstallServer

  def InstallServer.serve addr, port

    server_sock = TCPServer.new(addr, port)
    puts "install server running on port #{port}"
    
    thread_list = []

    loop do
      th = Thread.start(server_sock.accept) do |sock|
	sock.each_line do |line|
	  puts line
	end
      end
      thread_list << th

      if true # TODO close server after all nodes are installed
	break
      end
    end

    thread_list.each do |th|
      th.join
    end

  end
end

