# This is the controller for system settings.
#
# Author::    Santa Zhang (santa1987@gmail.com)
# Since::     0.3

require 'rubygems'
require 'rest_client'
require 'json'

class SettingsController < ApplicationController

  before_filter :root_required

  # Show a listing of settings.
  #
  # * params[:items]: a comma separated list of items to be shown, eg:
  #     /settings/index?items=id,key,value
  #   if params[:items] not given (nil), by default, 'key', 'value' and 'for_worker' are given
  #
  # for the valid 'items', check the definition for Setting in 'db/migrate' folder
  #
  # Since::   0.3
  def index
    if valid_param? params[:items]
      params[:items] = params[:items].split ',' # make sure params[:items] is an array, if provided
    else
      params[:items] = ["key", "value", "for_worker"]
    end
    reply_model Setting, :items => params[:items]
  end

  # Change system setting.
  # If the setting is also for worker machines, they will be updated, too.
  #
  # params:
  #   key: the key of a Setting
  #   value: the value of a Setting
  #
  # Since::   0.3
  def edit
    if params[:key] and params[:value]
      setting = Setting.find_by_key params[:key]
      if setting
        if setting.editable
          old_value = setting.value
          new_value = params[:value]
          setting.value = new_value

          # save new setting into database
          if setting.save
            reply_success "Successfully changed setting of #{params[:key]}.", :key => params[:key], :old_value => old_value, :new_value => new_value
          else
            reply_failure "Failed to save new setting!"
            return
          end

          # TODO sync data to workers (directly sync to workers)
        else
          reply_failure "Key '#{params[:key]}' is readonly!"
        end
      else
        reply_failure "Key '#{params[:key]}' was not found!"
      end
    else
      reply_failure "Please provide both 'key' and 'value'!"
    end
  end

end
