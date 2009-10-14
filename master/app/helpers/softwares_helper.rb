module SoftwaresHelper
	def SoftwaresHelper.get_soft_list(localpath)
		list = []

		Dir.foreach(localpath) do |dirname|
			fullpath = "#{localpath}/#{dirname}"
			description_file = "#{fullpath}/description"

			case dirname
				when ".", ".."
				else
					if File.directory?(fullpath) && File.exists?(description_file)
						des = `cat #{description_file}`
						list << { :software_name => dirname, :description => des}
					end
			end
		end			
		return list
	end

end
