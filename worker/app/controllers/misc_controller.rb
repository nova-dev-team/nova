class MiscController < ApplicationController

  def hi
    render_result :success => true, :message => "HI"
  end

  def whoami
    render_result :success => true, :message => "I'm worker", :role => "worker"
  end

end
