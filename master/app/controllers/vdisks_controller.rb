class VdisksController < ApplicationController

  before_filter :login_required

  def index
    result = []
    Vdisk.all.each do |vdisk|
      result << {
        :id => vdisk.id,
        :raw_name => vdisk.raw_name,
        :display_name => vdisk.display_name
      }
    end
    render_data result
  end

  def upload_list
    render_data VdisksHelper::Helper.upload_list
  end

	def soft_list
	  vd = Vdisk.find params[:id]
		result = []
		vd.software_categories.all.each do |sc|
			sc.softwares.all.each do |app|
			result << {
				:id => app.id,
				:category_id => sc.id,
				:display_name => app.display_name,
				:software_name => app.software_name
      }
			end
		end
		render_data result
	end

  def register
    # TODO add "base_upon" parameter
    if params[:upload_name] and params[:display_name] and params[:type]
      if params[:internal_name]
        internal_name = params[:internal_name]
      else
        internal_name = params[:display_name].gsub(" ", "_").gsub("\t", "_")
      end
      server_root = (Setting.find_by_key "storage_server").value
      if VdisksHelper::Helper.file_exist? "#{server_root}/vdisks_upload/#{params[:upload_name]}"
        vdisk = Vdisk.new
        vdisk.save # tricky, need to get an id first
        full_internal_name = "vd#{vdisk.id}-#{params[:type]}-#{internal_name}"
        vdisk.raw_name = full_internal_name
        vdisk.display_name = params[:display_name]
        vdisk.description = params[:comment]
        vdisk.save
        VdisksHelper::Helper.move_file server_root, "vdisks_upload/" + params[:upload_name], "vdisks/" + full_internal_name

        render_success "Successfully registered vdisk '#{params[:upload_name]}' as '#{full_internal_name}'"
      else
        render_failure "Cannot find file '#{params[:upload_name]}' on upload folder, check your spelling!"
        return
      end

    else
      render_error "You must provide 'upload_name', 'display_name' and 'type'!"
    end
  end

end
