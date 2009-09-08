module Rescue
	def Rescue.ignore
		begin
			yield
		rescue => e
		  puts "Error occurs, #{e.to_s}"
		  puts "  Ignored, continue working."
		end
	end
end
