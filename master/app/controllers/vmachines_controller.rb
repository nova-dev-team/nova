class VmachinesController < ApplicationController

  before_filter :login_required

  # vnc view
  def observe
    respond_to do |accept|
      accept.html {render :template => 'vmachines/observe'}
    end
  end

  def start
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.start
    render_success "Successfully started vmachine with UUID #{params[:uuid]}."
  end

  def stop
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.stop
    render_success "Successfully stopped vmachine with UUID #{params[:uuid]}."
  end

  def resume
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.resume
    render_success "Successfully resumed vmachines with UUID #{params[:uuid]}"
  end

  def suspend
    vm = Vmachine.find_by_uuid params[:uuid]
    vm.suspend
    render_success "Successfully suspended vmachine with UUID #{params[:uuid]}."
  end

end
