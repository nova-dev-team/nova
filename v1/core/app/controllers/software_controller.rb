class SoftwareController < ApplicationController

  def list
    result = []
    Software.all.each do |s|
      result << s.soft_name
    end
    respond_to do |accept| 
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def add
    soft = Software.new
    soft.soft_name = params[:id]
    soft.save
    result = {:success => true}
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

  def remove
    soft = Software.find_by_soft_name params[:id]
    if soft != nil
      result = {:success => true}
      Software.delete soft
    else
      result = {:success => false}
    end
    respond_to do |accept|
      accept.html {render :text => result.to_json}
      accept.json {render :json => result}
    end
  end

end
