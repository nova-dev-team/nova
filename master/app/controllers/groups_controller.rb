class GroupsController < ApplicationController

  include GroupsHelper
  def index
    respond_to do |accept|
      accept.json {
        render :text => Group.all.collect{|g| g.name}.to_json
      }
      accept.html {
        # render index.html.erb
      }
    end
  end

end
