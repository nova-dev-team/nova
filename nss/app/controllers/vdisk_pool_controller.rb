class VdiskPoolController < ApplicationController
  
  def register
    if (valid_param? params[:basename]) && (valid_param? params[:pool_size])
      @row = VdiskPool.find(:first, :conditions => ["basename = ?", params[:basename]])
      if @row != nil
        reply_failure "The template already exists!"
      else
        VdiskPool.add(params[:basename], params[:pool_size])
        #reply_success "Register successful!"
        reply_model VdiskPool
      end
    else
      reply_failure "Please input valid basename & pool_size!"
    end
  end

  def edit
    if (valid_param? params[:basename]) && (valid_param? params[:pool_size])
        VdiskPool.csize(params[:basename], params[:pool_size])
        # reply_success "Edit successful!"
        reply_model VdiskPool
    else
      reply_failure "Please input valid basename & pool_size!"
    end

  def unregister
    if valid_param? params[:basename]
      VdiskPool.del(params[:basename])
     # reply_success "Unregister successful!"
     reply_model VdiskPool
    end
  end

end
