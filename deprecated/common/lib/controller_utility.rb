# Helper module for both "master" and "worker" component.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3


require "#{File.dirname __FILE__}/utils.rb"
require 'cgi'

module ControllerUtility

private

  # Reply to a request.
  #
  # * success: boolean true or false
  # * message: text info about the result of the request
  # * addition_info: additional reply data, a hash table
  # bold: additional_info cannot contain key "success" or "message"
  #
  # example reply data in JSON:
  # {
  #   "success":true,
  #   "message":"your request is successful",
  #   "result":"blah"
  # }
  #
  # Since::     0.3
  def reply_result success, message, additional_info = {}
    if (additional_info.keys.include? :success) or (additional_info.keys.include? :message)
      raise "DO NOT use ':success' or ':message', they are preserved keys!"
    end
    result = {:success => success, :message => message}.merge(additional_info)
    respond_to do |accept|
      accept.xml {render :xml => result}
      accept.json {render :json => result}
      accept.html do
        # TODO better json result rendering
        render :text => <<HTML
<html>
<head>
<title>request result</title>
</head>
<body>
<div id="json_src">
#{CGI.escapeHTML(result.to_json)}
</div>
</body>
</html>
HTML
      end
    end
  end

  # Reply a failed request.
  #
  # Since::     0.3
  def reply_failure message, additional_info = {}
    reply_result false, message, additional_info
  end

  # Reply a successful request.
  #
  # Since::     0.3
  def reply_success message, additional_info = {}
    reply_result true, message, additional_info
  end

  # Render a all data of a given model. It is like running "select * from model".
  #
  # * you can select a given set of columns, by setting option[:items].
  #   example: option[:items] = [:id, :created_at, :updated_at]
  #
  # Since::     0.3
  def reply_model model, option = {}
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
    reply_success "query successful!", :data => data
  end

  # Check if parameter is valid (non-empty).
  #
  # Since::     0.3
  def valid_param? param
    param != nil and param != ""
  end

end
