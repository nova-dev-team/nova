class Setting < ActiveRecord::Base

  def Setting.default_storage_server
    (Setting.find_by_key "default_storage_server").value
  end

end

