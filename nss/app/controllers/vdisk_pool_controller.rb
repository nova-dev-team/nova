# The controller for maintaining VdiskPool.

class VdiskPoolController < ApplicationController

# register an image pool.
# * params[:basename]: file name of image template;
#   params[:pool_size]: size of image pool to be registered.
# eg: /vdisk_pool/register?basename=  &pool_size=
  def register
    if (valid_param? params[:basename]) && (valid_param? params[:pool_size])
      @row = VdiskPool.find(:first, :conditions => ["basename = ?", params[:basename]])
      #check if the same template exists.
      if @row != nil
        reply_failure "The template already exists!"
      else
        VdiskPool.add(params[:basename], params[:pool_size])
       # reply_success "Register successful!"
        reply_model VdiskPool
      end
    else
      reply_failure "Please input valid basename & pool_size!"
    end
  end

# modify size of image pool.
# * params[:basename]: name of image template;
#   params[:pool_size]: new size of image pool.
  def edit
    if (valid_param? params[:basename]) && (valid_param? params[:pool_size])
        row = VdiskPool.find(:first, :conditions => ["basename = ?", params[:basename]])
        if data != nil
          VdiskPool.csize(params[:basename], params[:pool_size])
          row = VdiskPool.find(:first, :conditions => ["basename = ?", params[:basename]])
        # reply_success "Edit successful!"
        reply_success "Query successful!", :data => data
        else
          reply_failure "The template not exists!"
        end
    else
      reply_failure "Please input valid basename & pool_size!"
    end
  end

# unregister image pool.
# * params[:basename]: name of image pool.
  def unregister
    if valid_param? params[:basename]
      VdiskPool.del(params[:basename])
     # reply_success "Unregister successful!"
     reply_model VdiskPool
     else
       reply_failure "please input valid basename!"
    end
  end

  def listvdisk
    reply_model VdiskPool
  end

end
