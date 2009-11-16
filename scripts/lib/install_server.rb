#!/usr/bin/ruby

require 'socket'
require 'thread'

class InstallServer

  def InstallServer.serve addr, port

    server_sock = TCPServer.new(addr, port)
    puts "install server running on port #{port}"
    
    thread_list = []

    client_status = {}
    node_list = []
    all_nodes do |node|
      client_status[node["intranet_ip"]] = "not_installed"
    end

    # terminator
    Thread.start do
      loop do
	assign_ip = nil
	client_status.each do |key, value|
	  if value == "not_installed"
	    assign_ip = key
	    break
	  end
	end
	if assign_ip == nil
	  puts "Finished installing all nodes"
	  exit! 0
	end
	sleep 1
      end
    end

    loop do
      th = Thread.start(server_sock.accept) do |sock|
	loop do
	  begin
	    line = sock.readline.chomp
	    puts line
	    if line.start_with? "get_client"
            # TODO add mutex!
	      assign_ip = nil
	      client_status.each do |key, value|
		if value == "not_installed"
		  assign_ip = key
		  client_status[key] = "assigned"
		  break
		end
	      end
	      puts "Found assign ip = #{assign_ip}"
	      client_ip = assign_ip
	      if client_ip == nil
	        sock.write "no_more_client\n"
	      else
		puts "Tell client to install " + assign_ip
	        sock.write "install #{assign_ip}\n"
	      end
	    else
	      sock.write "ack\n" # default ack message
	    end
	  rescue
	    # TODO client error?
	    break
	  end
        end
      end
      thread_list << th

    end

    thread_list.each do |th|
      th.join
    end

  end
end

