class SoftwaresController < ApplicationController
  before_filter :login_required

  def list_category
    result = []
    SoftwareCategory.all.each do |sc|
      result << {
        :id => sc.id,
        :name => sc.software_category_name
      }
    end
    render_data result
  end

  def list_available
    result = []
    Software.all.each do |app|
      result << {
        :id => app.id,
        :display_name => app.display_name,
        :software_name => app.software_name,
        :category_id => app.software_category.id
      }
    end
    render_data result
  end

  def destroy
    app = Software.find_by_id param[:id]
    app.destroy!
    render_success 'Successfully destroyed'    
  end

  def bind
    app = Software.find_by_id param[:id]
    sc = SoftwareCategory.find_by_id param[:cid]
    if app && sc
      app.software_category = sc
      render_success 'Successfully changes category'
    else
      render_failure 'Software or Category not found!'
    end
  end

  def scan
    package_setting = Setting.find_by_key "software_package_storage"
    if package_setting
      list = SoftwaresHelper.get_soft_list(package_setting.value)
      list.each do |app|
        ex = Software.find_by_software_name(app[:software_name])
        if !ex
          app_new = Software.new
          app_new.display_name = app[:software_name]
          app_new.software_name = app[:software_name]
          app_new.description = app[:software_name]
          app_new.software_category = SoftwareCategory.find_by_software_category_name 'incoming'
          if !app_new.save
            render_failure "Internel error...while scanning software packages on server:/#{package_setting.value}"
          end
        end        
      end
      render_success 'Scan complete'
    else
      render_failure 'Cannot find software_package_storage in Settings'
    end
  end

end


