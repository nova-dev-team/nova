class GroupsController < ApplicationController

  before_filter :require_admin_privilege

  include GroupsHelper
  def index
    result = Group.all.collect{|g| g.name}
    render_data result
  end

end
