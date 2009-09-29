class Software < ActiveRecord::Base
	belongs_to :software_category
	def Software.update_from_local
		localpath_setting = Setting.find_by_key("software_package_storage")
		
		if localpath_setting 
			soft_list = SoftwaresHelper.get_soft_list(localpath_setting.value)
		
			if soft_list 
				Software.delete_all
				soft_list.each do |app|
					soft = Software.new
					soft.software_name = app["software_name"]
					soft.description = app["description"]
					soft.save
				end
			end
		end


	end

end
