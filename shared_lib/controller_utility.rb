require "utils.rb"
require 'cgi'

# a helper module for both master & worker module
module ControllerUtility

private

  # report request result, either successful or failure
  def render_result success, message, option = {}
    if (option.keys.include? :success) or (option.keys.include? :message)
      raise "DO NOT use ':success' or ':message', they are preserved keys!"
    end
    result = {:success => success, :message => message}.merge(option)
    respond_to do |accept|
      accept.json {render :json => result}
      accept.html {render :text => CGI.escapeHTML(result.to_json)}
    end
  end

  # report failure
  def render_failure message, option = {}
    render_result false, message, option
  end

  # report success
  def render_success message, option = {}
    render_result true, message, option
  end

  # report for a successful query
  def render_data data
    render_result true, "Query successful.", :data => data
  end

  # report for a successful query, the data set is from a whole model
  #
  # option:
  #
  #  * option[:items] is a list of items to be rendered. eg:
  #
  #      option[:items] = [:id, :created_at, :updated_at]
  #
  #    if option[:items] is not given, by default, all items will be rendered
  #
  def render_model model, option = {}
    all_columns = model.columns.collect {|column| column.name}
    if option[:items] == nil
      option[:items] = all_columns
    else
      option[:items] = option[:items].select {|item| all_columns.include? item} # remove non-existing keys
    end

    data = model.all.collect do |row|
      row_data = {}
      option[:items].each {|item| row_data[item] = row[item]}
      row_data
    end
    render_data data
  end

  def valid_param? param
    param != nil and param != ""
  end

end
