require 'fileutils'

class FsController < ApplicationController
 
  def  listdir
    if valid_param? params[:dir]
      dir = "#{RAILS_ROOT}/../../storage"+params[:dir]
      data = "["
       Dir.foreach(dir.to_s) do |entry|
         if File.extname(dir.to_s + "/" + entry.to_s) == ".qcow2"
            data += "{ filename: " + entry.to_s + ", "
            fsize = File.size(dir.to_s + "/" + entry.to_s)
            data += "size: " + fsize.to_s  + ", " + "is_dir: "
            if File.ftype(dir.to_s + "/" + entry.to_s) == "directory"
               data += "true }"
            else
               data += "false }"
            end 
          end
        end
       data += "]"
    reply_success "Query successful! Data: #{data}"
    end
  end


  def rm
    if valid_param? params[:path]
      path = params[:path]
      File.delete(path.to_s)
      reply_success " Delete successful!"
    end
  end

  def mv
    if (valid_param? params[:from]) && (valid_param? params[:to])
      from = params[:from]
      to = params[:to]
      FileUtils.mv(from.to_s, to.to_s)
      reply_success "Remove successful!"
    end
  end

  def cp
    if (valid_param? params[:from]) && (valid_param? params[:to])
      from = params[:from]
      to = params[:to]
     fork do
        File.new(to.to_s + ".copying", "w")
        system("cp #{from.to_s} #{to.to_s}")
        File.delete(to.to_s + ".copying")
      end
    reply_success "Copy successful!"
    end 
  end

end
