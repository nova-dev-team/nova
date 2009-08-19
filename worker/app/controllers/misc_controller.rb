class MiscController < ApplicationController

  def hi
    render :text => "HI"
  end

  def whoami
    render :text => "Worker"
  end

end
