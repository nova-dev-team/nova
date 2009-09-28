class SettingsController < ApplicationController

  # show a listing of settings
  #
  # * params[:items]: a comma separated list of items to be shown, eg:
  # 
  #     /settings/index?items=id,key,value
  #
  #   if params[:items] not given (nil), by default, 'key' and 'value' are given
  #
  def index
    if valid_param? params[:items]
      params[:items] = params[:items].split ',' # make sure params[:items] is an array, if provided
    else
      params[:items] = ["key", "value"]
    end
    render_model Setting, :items => params[:items]
  end
 
  # edit a setting
  #
  # * params[:key]: key for the item to be edited
  # * params[:value]: new value for the item
  #   both params are REQUIRED
  #
  def edit
    key = params[:key]
    new_value = params[:value]

    unless (valid_param? key) and (valid_param? new_value)
      render_failure "Please provide both 'key' and 'value'!"
      return
    end

    setting = Setting.find_by_key params[:key]
    unless setting
      render_failure "Key '#{key}' not found!"
      return
    end

    unless setting.editable
      render_failure "Key '#{key}' is not editable!"
      return
    end

    old_value = setting.value
    setting.value = new_value
    unless setting.save
      render_failure "Failed to save changes to key '#{key}'!"
      return
    end

    render_success "Successfully changed setting for '#{key}'.", :old_value => old_value, :new_value => new_value
  end

end

