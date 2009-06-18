class OkController < ApplicationController

  protect_from_forgery :except => :create

  def list
    @list_of_vm = ["vm1", "vm2", "vm3"]
    @vm = Vm.new
  end

  def start
    render :text => "#{params[:id]} start ok"
  end

  def stop
    render :text => "#{params[:id]} stop ok"
  end

  def suspend
    render :text => "#{params[:id]} suspend ok"
  end

  def resume
    render :text => "#{params[:id]} resume ok"
  end

  def create
    output = "#{params[:id]} create ok</br>\n"
    params.each do |k, v|
      output += "#{k}, #{v}</br>\n"
    end
    vm = Vm.new
    vm.update(params[:vm])
    output += "#{vm.name}</br>\n"
    output += "#{vm.uuid}</br>\n"
    output += "#{vm.cpu}</br>\n"
    output += "#{vm.mem}</br>\n"
    output += "#{vm.desc}</br>\n"
    output += "#{vm.blah}</br>\n"


    render :text => output
  end

  def check
    render :text => "#{params[:id]} check ok"
  end

  def destroy
    render :text => "#{params[:id]} destroy ok"
  end

end
