require 'socket'


class CeilPorter
  def initialize agent_ip, agent_password, pkg_server_addr
    @agent_addr = agent_ip
    @agent_password = agent_password
    @pkg_server_addr = pkg_server_addr
  end

  def add_pkg pkg_name
    vm_conn = TCPSocket::new(agent_ip, 32167)

  end

  def status
  end

end

