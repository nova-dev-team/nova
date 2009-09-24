# controller for the "ceil" submodule by Huang Gang
# "ceil" is used to automatically install & configure vmachines
require "ceil_conf"
require "pp"
class CeilController < ApplicationController

  # TODO by santa: login required? what about the safety of ceil controller? If anonymous user could controll Ceil, there might be security problems

  # login not required, if virtual machine has fixed IP
  #	see: remote_ip = request.remote_ip
	def test
	    remote_ip = request.remote_ip
	    remote_ip = remote_ip.chomp
	    vm = Vmachine.all #find_by_ip(remote_ip)
	    miao = Vmachine.find_by_ip(remote_ip)
  	     
	    context = ""
	    context += "your ip = #{remote_ip}<br>"

	    if miao
		context += "vmachine found! id = #{miao.id}<br>"
		context = context + miao.hostname + " " + miao.ip + " " + miao.vcluster_id.to_s + "<br>"

		vc = miao.vcluster
		if vc
		    context += "good , vcluster name = #{vc.cluster_name}, list = #{vc.package_list}<br>"
		else
		    context += "oh shit the vm do not have a vcluster<br>"
		end
	    else
		context = context + "fucked ! nil !!!<br>"
	    end

	    context += "now list all vms<br>"
	    vm.each do |blah|
		context = context + " " + blah.id.to_s + " " + blah.ip + " " + blah.hostname+ " " + blah.vcluster_id.to_s + " " + "<br>"
	    end

	    context += "now list all clusters<br>"
	    vcc = Vcluster.all
	    vcc.each do |bla|
		context += " " + bla.id.to_s + " " + bla.cluster_name + " " + bla.package_list + "<br>"
	    end
	    respond_to do |accept|
		accept.html { render :text => context }
	    end
	end
  
	def retrieve
		respond_to do |accept|
		  remote_ip = request.remote_ip
		  
		  vm = Vmachine.find_by_ip(remote_ip)
		  #logger.debug "found: " + vm.to_s
		  host_name = ""
		  node_list = ""
		  server_addr = ""
		  package_list = ""
		  cluster_name = ""
			character = []

		  if vm
		    host_name = vm.hostname
		    node_list = vm.get_node_list
		    server_addr = vm.vcluster.net_segment.head_ip
		    package_list = vm.get_package_list
		    cluster_name = vm.vcluster.cluster_name
		  end

		  character << "worker"
			character << "master" if vm.master?

		  cjson = {"host_name" => host_name, 
		           "cluster_name" => cluster_name, 
		           "node_list" => node_list, 
		           "package_list" => package_list,
		           "package_server" => server_addr,
		           "package_server_type" => CEIL_PACKAGE_SERVER_TYPE,
		           "key_server" => server_addr,
		           "key_server_type" => CEIL_KEY_SERVER_TYPE,
							 "character" => character} 
		  
			accept.json { render :json => cjson }
			accept.html { render :text => cjson.to_json }
			
			#accept.ip { render :text => request.remote_ip() }
			#accept.config { render :text => request.remote_ip() }
		end			

	end
	
	def report
	  return
	# log_type & log_msg
	  pp = params
	  log_status = pp[:log_status].chomp.to_i
	  log_category = pp[:log_category].chomp
	  log_message = pp[:log_message].chomp
	  #logger.debug "----BEGIN----"
	  #logger.debug "#{log_type} : #{log_msg}"
	  #logger.debug "-----END-----"
	  
	  remote_ip = request.remote_ip
	  vm = Vmachine.find_by_ip(remote_ip)
	  if vm 
	    log = VmachineInfo.new
	    
	    log.status = log_status
	    log.category = log_category
	    log.message = log_message
	    log.vmachine = vm
	    log.save
	    
	    count = 0
	    order = 0
	    found = false
	    vm.get_package_list.split.each do |package|
	      logger.debug "package = #{package}, current = #{log_category}"
	      count += 1
	      found = true if (package == log_category)
	      order += 1 if !found
	    end
	    
	    case log_status
	      when 0
	        vm.ceil_progress = 1
	      when 100
	        vm.ceil_progress = 100
	      when [1..99]
	        full = 2 + count * CEIL_APP_INSTALL_STEPS
	        current = 1 + order * CEIL_APP_INSTALL_STEPS
	        step = CEIL_APP_INSTALL_STEPS * log_status / CEIL_APP_STATUS_MAX
	        vm.ceil_progress = 100 * (current + step) / full
	        logger.debug "count = #{count}"
	        logger.debug "order = #{order}"
	        logger.debug "log_status = #{log_status}"
	        logger.debug "vm.ceil_progress = #{vm.ceil_progress}"
	      else
	        logger.debug "error"
	    end
	    vm.last_ceil_message = CeilMessage.message(log_status, log_category)
	    logger.debug vm.last_ceil_message
	    vm.save
	  end
	  
    respond_to do |accept|
      #accept.json { render :json => "[#{log_type}]#{log_msg}" }
      accept.html { render :text => "[#{log_category}]#{log_message}\n#{vm.ceil_progress}%\n#{vm.last_ceil_message}" }
    end
	end
end



