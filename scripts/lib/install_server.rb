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
	loop do
	  line = sock.readline
	  puts line
	  if line == "get_client"
	    sock.write "no more client\n"
	  else
	    sock.write "ack\n" # default ack message
	  end
	  sock.flush
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

