require File.dirname(__FILE__) + '/server_settings'

class KeyDispatcher
  def initialize(cluster_name, app_name)
    @cluster_name = cluster_name
    @app_name = app_name

    @disp_path = "#{SERVER_KEY_DISPATCH_PATH}/#{@cluster_name}/#{@app_name}"
    @keys_path = "#{SERVER_KEY_STORE_PATH}/#{@app_name}"

    system "mkdir #{SERVER_KEY_DISPATCH_PATH}/#{@cluster_name}"
    system "mkdir #{SERVER_KEY_DISPATCH_PATH}/#{@cluster_name}/#{@app_name}"
  end

  def dispatch(node_list)
    #node_list = `cat #{node_list_file}`.chomp

     node_count = 0
     master = ""
    node_list.each_line do |line|
      master = line if node_count == 0
       node_count += 1
     end

    if File.exists?("#{@keys_path}/multi")
    else
      node_count = 1
      node_list = master
    end

    available_count = 0
    key_path = SERVER_KEY_STORE_PATH + "/#{@app_name}/available"
    key_list = `ls #{key_path}`.chomp.split
    key_list.each do |key|
      #remain = `cat #{key_path}/#{key}/remain`.chomp.to_i
      available_count += 1
      #+=remain
    end	

    if available_count >= node_count
      system "cp -r #{@keys_path}/attach/* #{@disp_path}"
      node_list.each_line do |line|
        ip, host_name = line.split
        #puts ip
        #puts host_name
        system "rm -r #{@disp_path}/#{host_name}"
        key = key_list.pop
        system "cp -r #{@keys_path}/available/#{key}/key #{@disp_path}/#{host_name}"
        log = `date` + "Dispatched to #{host_name}(#{ip}) in cluster #{@cluster_name}\n\n"
        File.open("#{@keys_path}/available/#{key}/history", "a") do |file|
          file.puts(log)
        end
        remain = `cat #{key_path}/#{key}/remain`.chomp.to_i - 1
        #puts "Remains = #{remain}"

        #system "mv -r #{@keys_path}/available/#{key} #{@keys_path}/used/"
      end
    else
      puts "oh there is not enough keys, plz contact admin"
    end
  end
end

