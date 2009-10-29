class SoftwareCategoriesController < ApplicationController

	def index
		result = []
		SoftwareCategory.all.each do |sc|
			result << {
				:id => sc.id,
				:name => sc.software_category_name
			}
		end
		render_data result
	end

	def list
		result = []
		sc = SoftwareCategory.find_by_id params[:id]
		if sc
			sc.softwares.each do |app|
				result << {
				:id => app.id,
				:display_name => app.display_name,
				:software_name => app.software_name,
				:category_id => app.software_category.id
			}
			end
		end
		render_data result
	end

	def add
		name = params[:name]
		sc = SoftwareCategory.find_by_software_category_name(name)
		if sc
			render_failure "Name #{name} already exists!"
		else
			cate = SoftwareCategory.new
			cate.software_category_name = name
			if cate.save
				render_success "Category #{name} added"
			else
				render_failrue "Database error"
			end
		end
	end	

	def destroy
		sc = SoftwareCategory.find_by_id params[:id]
		sc.softwares.each do |app|
			app.destroy
		end
		sc.destroy
		render_success "Category destroyed"
	end

end


