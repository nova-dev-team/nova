module VimageHelper

  class Helper
    
    def Helper.list
      result = []
      Vimage.all.each do |vimage|
        result << {
          :os_family => vimage.os_family,
          :os_name => vimage.os_name,
          :hidden => vimage.hidden
        }
      end
      return result
    end

    def Helper.add os_family, os_name
      result = {}

      os_family = "other" if os_family == nil
      if os_name == nil
        result[:success] = false
        result[:msg] = "Not enough information!"

      else
        result[:success] = true
        vimage = Vimage.new
        vimage.os_family = os_family
        vimage.os_name = os_name
        vimage.save
        result[:os_family] = os_family
        result[:os_name] = os_name
        result[:iid] = "i#{vimage.id}"
        result[:msg] = "Added new vimage"

      end
      return result
    end

    def Helper.hide iid
      result = {}
      vimage = Vimage.find_by_id iid[1..-1]
      if vimage != nil # vimage found
        if vimage.hidden == true # already hidden
          result[:success] = false
          result[:msg] = "Vimage #{iid} already hidden!"

        else # vimage not hidden
          vimage.hidden = true
          vimage.save
          result[:success] = true
          result[:msg] = "Vimage #{iid} is now hidden"
        end

      else # vimage not found
        result[:success] = false
        result[:msg] = "Vimage #{iid} not found!"
      end

      return result
    end

    def Helper.unhide iid
      result = {}
      vimage = Vimage.find_by_id iid[1..-1]
    end

  end
end
