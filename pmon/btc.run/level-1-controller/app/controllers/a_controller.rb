class AController < ApplicationController
  protect_from_forgery :except => :post


  def admin
  end

  def auth
    render :text => "ok"
  end

  def post
    puts params
    h = eval(params[:from])
    puts h.class
    puts h.keys
    puts h.values
    params.each_pair { |k, v|
      puts "#{k} / #{v}"
    }
    render :text => params
  end
end
