# The controller for maintaining VdiskPool.

class VdiskPoolController < ApplicationController


  # List all the vdisk image pool.
  #
  # Since::   0.3
  def list
    reply_model VdiskPool, :items => ["basename", "pool_size"]
  end

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
        reply_success "Successfully registered '#{params[:basename]}' into image pool, with pool size '#{params[:pool_size]}'!", :basename => params[:basename], :pool_size => params[:pool_size]
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
        @row = VdiskPool.find(:first, :conditions => ["basename = ?", params[:basename]])
        if @row != nil
          VdiskPool.csize(params[:basename], params[:pool_size])
        # reply_success "Edit successful!"
          reply_success "Successfully modified pool size of '#{params[:basename]}' to #{params[:pool_size]}", :basename => params[:basename], :pool_size => params[:pool_size]
        else
          reply_failure "The template not exists!"
        end
      VdiskPool.csize(params[:basename], params[:pool_size])
      # reply_success "Edit successful!"
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
      reply_success "Successfully unregistered '#{params[:basenem]}' from image pool."
    else
      reply_failure "please input valid basename!"
    end
  end

# list all registered vdisks.
  def listvdisk
    reply_model VdiskPool
  end

end
