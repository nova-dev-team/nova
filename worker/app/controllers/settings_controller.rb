class SettingsController < ApplicationController

  def index
    settings = {}
    Setting.all.each do |setting|
      settings[setting.key] = setting.value
    end
    render_result settings
  end
  
  def show
    if params[:key]
      setting = Setting.find_by_key params[:key]
      if setting
        result = {
          :success => true,
          :message => "Query succeeded.",
          :value => setting.value,
          :no_edit => setting.no_edit
        }
        render_result result
      else
        render_failure "Key '#{params[:key]}' was not found!"
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
            result = {
              :success => true,
              :message => "Successfully changed setting.",
              :old_value => old_value,
              :new_value => new_value
            }
            render_result result
          else # save failed
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
  end

end

