class Setting < ActiveRecord::Base

  # get all settings that are intended for worker
  def Setting.all_for_worker
    Setting.find_all_by_for_worker true
  end

end
