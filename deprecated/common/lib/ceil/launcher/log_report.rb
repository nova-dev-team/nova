require 'net/http'

class LogReporter
  def initialize(server_addr, local_addr = "")
    @server_addr = server_addr
    @local_addr = local_addr
  end

  def send(log_status, log_category, log_message = "")
    conn = Net::HTTP.new(@server_addr, 3000)
    log_status = log_status.gsub(/\s/, '_')
    log_category = log_category.gsub(/\s/, '_')
    log_message = log_message.gsub(/\s/, '_')

    begin
  		resp, data = conn.post("/ceil/report?rip=#{@local_addr}&log_status=#{log_status}&log_category=#{log_category}&log_message=#{log_message}", nil)
  	rescue => e
  	  puts "Error during sending log to nova/ceil server, #{e.to_s}"
  	  #return nil
  	end
  	return 0
  end
end
