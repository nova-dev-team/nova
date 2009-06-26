# This controller is used to display general information of the nova platform

class AboutController < ApplicationController

  def author
    respond_to do |accept|
      accept.json {
        render :text => {
          :name => "Santa Zhang",
          :email => "santa.zh@gmail.com",
          :project_website => "http://www.github.com/santazhang/nova"
        }.to_json
      }
    end
  end

  def status
    respond_to do |accept|
      accept.json {
        render :text => {
          :health => "healthy"
        }.to_json
      }
    end
  end

  def version
    respond_to do |accept|
      version_str = "0.2-alpha"

      accept.json {
        render :text => {
          :version => version_str
        }.to_json
      }

      accept.html {
        render :text => version_str
      }
    end
  end

end
