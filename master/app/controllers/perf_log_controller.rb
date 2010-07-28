class PerfLogController < ApplicationController
  def show
    reply_success "Query successful!", :data => PerfLog.find_all_by_pmachine_id(params[:pm_id])
  end
end
