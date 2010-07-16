class PerfLogController < ApplicationController
  def show
    PerfLog.find_all_by_pmachine_id(params[:pm_id])
  end
end
