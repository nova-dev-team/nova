class CeilController < ApplicationController
	def retrieve

		respond_to do |accept|
			accept.json { render :json => request.remote_ip() }
			accept.html { render :text => request.remote_ip().to_json }
			
			#accept.ip { render :text => request.remote_ip() }
			#accept.config { render :text => request.remote_ip() }
		end			

	end
	def report
				


	end
end
