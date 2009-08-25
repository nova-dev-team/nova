# controller for the "ceil" submodule by Huang Gang
# "ceil" is used to automatically install & configure vmachines

class CeilController < ApplicationController

	def retrieve
		respond_to do |accept|
		  remote_ip = request.remote_ip
		  
		  vm = Vmachine.find_by_ip(remote_ip)
		  node_list = ""
		  package_list = ""
		  if vm 
		    node_list = vm.get_node_list
		    package_list = vm.get_package_list
		  end
		  
		  cjson = {"node_list" => node_list, "package_list" => package_list} 
		  
			accept.json { render :json => cjson }
			accept.html { render :text => cjson.to_json }
			
			#accept.ip { render :text => request.remote_ip() }
			#accept.config { render :text => request.remote_ip() }
		end			

	end
	
	def report
			


	end
end
