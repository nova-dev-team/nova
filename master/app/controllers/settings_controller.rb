# system settings, determines system behavior

class SettingsController < ApplicationController

  before_filter :render_only_for_root

  def index
    settings = {}
    Setting.all.each do |setting|
      settings[setting.key] = setting.value
    end
    render_result true, "Query succeeded.", :data => settings
  end

  def show
    if params[:key]
      setting = Setting.find_by_key params[:key]
      if setting
        render_result true, "Query succeeded.", :value => setting.value, :no_edit => setting.no_edit
      else
        render_failure "Key '#{params[:key]}' not found!"
      end
    else
      render_failure "No key was given!"
    end
  end

  def edit
    if params[:key] and params[:value]
      setting = Setting.find_by_key params[:key]
      if setting
        if setting.no_edit == false
          old_value = setting.value
          new_value = params[:value]
          setting.value = new_value
          if setting.save
            render_result true, "Successfully changed setting.", :old_value => old_value, :new_value => new_value
          else
            render_failure "Failed to save new setting!"
          end
        else
          render_failure "Key '#{params[:key]}' is readonly!"
        end
      else
        render_failure "Key '#{params[:key]}' was not found!"
      end
    else
      render_failure "Please provide both key and value!"
    end
    # TODO sync data to workers (directly sync to workers)
  end

  def push_to_workers
    # TODO async push data to workers
  end

private

  def render_only_for_root
    unless current_user and current_user.in_group? "root"
      render_error_no_privilege
    end
  end

end
