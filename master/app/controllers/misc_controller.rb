# A helper controller that provides lots of utilities.
#
# Author::      Santa Zhang (mailto:santa1987@gmail.com)
# Since::       0.3

require 'utils'

class MiscController < ApplicationController

  # Reply the role of this node.
  #
  # Since::   0.3
  def role
    reply_success "master"
  end

  # Reply the current version of Nova platform.
  #
  # Since::   0.3
  def version
    if File.exists? "#{RAILS_ROOT}/../VERSION"
      ver = File.read("#{RAILS_ROOT}/../VERSION").strip
      reply_success "Version is '#{ver}'", :version => ver
    else
      reply_failure "Version unknown!"
    end
  end

  # Returns the running Rails environment.
  #
  # Since::   0.3
  def rails_env
    reply_success "Rails environment is '#{Rails.env}'", :env => Rails.env
  end

  # Reply the role of current user.
  # Possible return values: "root", "admin", "normal user". If user not logged in, an failure will be returned.
  #
  # Since::   0.3
  def my_privilege
    if logged_in?
      priv = @current_user.privilege
      reply_success "Your privilege is '#{priv}'", :privilege => priv
    else
      reply_failure "You are not logged in!"
    end
  end

  # Reply detailed information of current user.
  # If user not logged in, an failure will be replied.
  #
  # Since::   0.3
  def who_am_i
    if logged_in?
      reply_success "Your info is successfully retireved.",
        :privilege => @current_user.privilege,
        :login => @current_user.login,
        :name => @current_user.name,
        :email => @current_user.email
    else
      reply_failure "You are not logged in!"
    end
  end


  # Use the 'port_mapper' tool to add a port forwarding.
  # Adapted from "vm_pool" module.
  # Requies those parameters: "local_port", "ip", "port".
  #
  #  * local_port:  on which port of local machine should the forwarder listen to
  #  * ip:          the destination ip
  #  * port:        the destination port
  #
  # Since::     0.3
  def add_port_mapping
    return unless root_required
    return unless valid_ip_and_port_param?

    unless valid_param? params[:local_port] and valid_param? params[:ip] and valid_param? params[:port]
      reply_failure "Please provide 'local_port', 'ip', 'port' information!"
      return
    end

    if params[:local_port].to_i.to_s != params[:local_port]
      reply_failure "Please provide a valid 'local_port'!"
      return
    end
    local_port = params[:local_port].to_i

    fwd_addr = "#{params[:ip]}:#{params["port"]}"
    port_file = "#{RAILS_ROOT}/log/#{fwd_addr.gsub ":", "_"}.local_port"

    if File.exists? port_file
      local_port = File.read(port_file).to_i
    else
      timeout = 0 # forever available
      my_exec "#{RAILS_ROOT}/lib/port_fwd_daemon #{timeout} #{local_port} #{fwd_addr} #{RAILS_ROOT}/log #{RAILS_ROOT}/../tools/server_side/bin/port_mapper"
    end
    reply_success "mapping added", :proxy_port => local_port, :dest_ip => params[:ip], :dest_port => params[:port]
  end

  # List all the available port mappings.
  # Adapted from "vm_pool" module.
  #
  # Since::     0.3
  def list_port_mapping
    return unless root_required
    mappings = []
    Dir.foreach("#{RAILS_ROOT}/log") do |entry|
      next unless entry =~ /\.pid$/
      pid = File.read("#{RAILS_ROOT}/log/#{entry}").to_i
      begin
        Process.kill 0, pid
      rescue
        next
      end
      main_fn = entry[0..-5]
      next unless main_fn =~ /^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+_[0-9]+$/
      port_fn = "#{RAILS_ROOT}/log/#{main_fn}.local_port"
      if File.exists? port_fn
        mappings << {
          :local_port => File.read(port_fn).to_i,
          :port => main_fn.split("_")[1].to_i,
          :ip => main_fn.split("_")[0]
        }
      else
        # kill broken mappings
        kill_by_pid_file "#{RAILS_ROOT}/log/#{entry}"
      end
      
    end
    reply_success "query successful", :data => mappings
  end

  # Remove an existing port mapping.
  # Adapted from "vm_pool" module.
  #
  # Since::     0.3
  def del_port_mapping
    return unless root_required
    return unless valid_ip_and_port_param?
    fwd_addr = "#{params[:ip]}:#{params["port"]}"
    port_file = "#{RAILS_ROOT}/log/#{fwd_addr.gsub ":", "_"}.local_port"
    pid_file = "#{RAILS_ROOT}/log/#{fwd_addr.gsub ":", "_"}.pid"

    if File.exists? pid_file
      kill_by_pid_file pid_file
      reply_success "The mapping is deleted."
    else
      reply_failure "The port mapping was not found."
    end

    if File.exists? port_file
      FileUtils.rm port_file
    end
  end

private

  # Check if the parameters for port mapper is correct.
  # Adapted from "vm_pool" module.
  #
  # Since::   0.3
  def valid_ip_and_port_param?
    unless valid_param? params[:ip] and params[:ip].is_ip_addr?
      reply_failure "Please provide a valid 'ip'."
      return false
    end
    unless valid_param? params[:port] and params[:port].to_i.to_s == params[:port]
      reply_failure "Please provide a valid 'port'."
      return false
    end
    return true
  end

end
