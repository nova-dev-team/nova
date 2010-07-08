require 'fileutils'

class FsController < ApplicationController
  def  listdir
    if valid_param? params[:dir]
      dir = "#{RAILS_ROOT}/../../storage"+params[:dir]
      list = ""
       Dir.foreach(dir.to_s) do |entry|
         list += entry.to_s
         File.umask()
       end
    reply_success "List: #{list}"
    end
#   render :text => params[:path] 
 #   p = params[:dir]
  #  reply_success "Blah #{p}"
#render :text => Dir.entries(dir.to_s).join("   ")
  end
end
