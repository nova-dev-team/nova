# Controller for settings model.
#
# Author::    Santa Zhang (mailto:santa1987@gmail.com)
# Since::     0.3

class SettingsController < ApplicationController

  # Show a listing of settings.
  #
  # * params[:items]: a comma separated list of items to be shown, eg:
  #     /settings/index?items=id,key,value
  #   if params[:items] not given (nil), by default, 'key' and 'value' are given
  #
  # Since::   0.3
  def index
    if valid_param? params[:items]
      params[:items] = params[:items].split ',' # make sure params[:items] is an array, if provided
    else
      params[:items] = ["key", "value"]
    end
    reply_model Setting, :items => params[:items]
  end

  # Show the value for a single item.
  #
  # Since::     0.3
  def show
    if valid_param? params[:key]
      s = Setting.find_by_key params[:key]
      if s == nil
        reply_failure "failed to find setting with name '#{params[:key]}'"
      else
        reply_success "query successful!", :value => s.value
      end
    else
      reply_failure "please provide the 'key'!"
    end
  end

  # Edit a setting value.
  #
  # * params[:key]: key for the item to be edited
  # * params[:value]: new value for the item
  #   both params are REQUIRED.
  #
  # Since::   0.3
  def edit
    key = params[:key]
    new_value = params[:value]

    unless (valid_param? key) and (valid_param? new_value)
      reply_failure "Please provide both 'key' and 'value'!"
      return
    end

    setting = Setting.find_by_key params[:key]
    unless setting
      reply_failure "Key '#{key}' not found!"
      return
    end

    unless setting.editable
      reply_failure "Key '#{key}' is not editable!"
      return
    end

    old_value = setting.value
    setting.value = new_value
    unless setting.save
      reply_failure "Failed to save changes to key '#{key}'!"
      return
    end

    reply_success "Successfully changed setting for '#{key}'.", :old_value => old_value, :new_value => new_value
  end

end

