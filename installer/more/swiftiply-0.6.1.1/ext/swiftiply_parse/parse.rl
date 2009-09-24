/*				def add_frontend_client clnt
					clnt.create_time = @ctime
					unless match_client_to_server_now(clnt)
						if clnt.uri =~ /\w+-\w+-\w+\.\w+\.[\w\.]+-(\w+)?$/
							@demanding_clients[$1].unshift clnt
						else
							@client_q[clnt.name].unshift(clnt)
						end
					end
				end

				def match_client_to_server_now(client)
					sq = @server_q[@incoming_map[client.name]]
					if client.uri =~ /\w+-\w+-\w+\.\w+\.[\w\.]+-(\w+)?$/ and sidx = sq.index(@reverse_id_map[$1])
						server = sq.delete_at(sidx)
						server.associate = client
						client.associate = server
						client.push
						true
					elsif server = sq.pop
						server.associate = client
						client.associate = server
						client.push
						true
					else
						false
					end
				end
*/
	
/*
			def receive_data data
				@data.unshift data
				if @name
					push
				else
					data =~ /\s([^\s\?]*)/
					@uri ||= $1
					if data =~ /^Host:\s*([^\r\n:]*)/
						@name = $1
						ProxyBag.add_frontend_client self
						push
					elsif data.index(/\r\n\r\n/)
						@name = ProxyBag.default_name
						ProxyBag.add_frontend_client self
						push
					end
				end
			end
*/

/*


			def receive_data data
				unless @initialized
					@id = data.slice!(0..11)
					ProxyBag.add_id(self,@id)
					@initialized = true
				end
				unless @headers_completed 
					if data.index(Crnrn)
						@headers_completed = true
						h,d = data.split(Rrnrn)
						@headers << h << Crnrn
						@headers =~ /Content-Length:\s*([^\r\n]+)/
						@content_length = $1.to_i
						@associate.send_data @headers
						data = d
					else
						@headers << data
					end
				end

				if @headers_completed
					@associate.send_data data
					@content_sent += data.length
					if @content_sent >= @content_length
						@associate.close_connection_after_writing
						@associate = nil
						setup
						ProxyBag.add_server self
					end
				end
			rescue
				@associate.close_connection_after_writing
				@associate = nil
				setup
				ProxyBag.add_server self
			end
