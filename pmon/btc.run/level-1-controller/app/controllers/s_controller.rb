
require(File.join '../shared-lib/' + "utils")

# 本地系统镜像管理。
#    * 系统镜像来自中央的服务器，用于虚拟机的系统盘。
#    * 系统镜像不被修改，使用时需要被克隆。
#    * 可以将使用过的克隆建立伪新的系统镜像。
#    * 新的系统镜像可以被传输回远程的存储服务器。
class SController < ApplicationController

  include Utils

  skip_before_filter :verify_authenticity_token
  # protect_from_forgery :only => :create

  # 检索本地现有的镜像, 包括本地的和系统的。
  #  list  => a array of vdisks
  def list_se
    @vds = Vdisk.find(:all)
    respond_to do |accept|
      accept.html
      accept.json { render :json => @vds}
    end
  end

  # 上传本地的系统镜像到服务器
  #   upload id, url =>
  def upload
      respond_to do |accepts|
        accepts.html {render :text => "Not Implemented", :status => :not_implemented}
        accepts.json {render :json => "Not Implemented", :status => :not_implemented}
      end
  end

  # 察看一个存储引擎的状态
  #   check_se => :ok | :gone
  def check_se
    if call_bus('/cn/org/btc/StorageEngine') {|bus| bus.check } == ['true']
      respond_to do |accepts|
        accepts.html {render :text => "Storage Engine is running"}
        accepts.json {render :json => "Storage Engine is running"}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "存储引擎未运行", :status => :gone}
        accepts.json {render :json => "存储引擎未运行", :status => :gone}
      end
    end
  end

  # 如果存储引擎在运行，启动他。
  # 虽然此服务立刻返回，但存储引擎不一定立即启动，需要等待他完成了手头的工作后才会停止运行。
  #   stop_se => :ok
  def stop_se
    if call_bus('/cn/org/btc/StorageEngine') {|bus| bus.stop }
      respond_to do |accepts|
        accepts.html {render :text => "Stoped the storage engine"}
        accepts.json {render :json => "Stoped the storage engine"}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "未初始化存储引擎", :status => :bad_request}
        accepts.json {render :json => "未初始化存储引擎", :status => :bad_request}
      end
    end
  end

  # 存储引擎未启动，启动他。
  #   restart_se =>
  def restart_se
    if call_bus('/cn/org/btc/StorageEngine') {|bus| bus.start }
      respond_to do |accepts|
        accepts.html {render :text => "Start the storage engine"}
        accepts.json {render :json => "Start the storage engine"}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "未初始化存储引擎", :status => :bad_request}
        accepts.json {render :json => "未初始化存储引擎", :status => :bad_request}
      end
    end
  end

  # 察看一个存储引擎的状态
  #   check_ue => :ok | :gone
  def check_ue
    if call_bus('/cn/org/btc/UpdateEngine') {|bus| bus.check } == ['true']
      respond_to do |accepts|
        accepts.html {render :text => "Update Engine is running"}
        accepts.json {render :json => "Update Engine is running"}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "更新引擎未运行", :status => :gone}
        accepts.json {render :json => "更新引擎未运行", :status => :gone}
      end
    end
  end

  # 如果存储引擎在运行，启动他。
  # 虽然此服务立刻返回，但存储引擎不一定立即启动，需要等待他完成了手头的工作后才会停止运行。
  #   stop_ue => :ok
  def stop_ue
    if call_bus('/cn/org/btc/UpdateEngine') {|bus| bus.stop }
      respond_to do |accepts|
        accepts.html {render :text => "Stoped the update engine"}
        accepts.json {render :json => "Stoped the update engine"}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "未初始化更新引擎", :status => :bad_request}
        accepts.json {render :json => "未初始化更新引擎", :status => :bad_request}
      end
    end
  end

  # 存储引擎未启动，启动他。
  #   restart_ue =>
  def restart_ue
    if call_bus('/cn/org/btc/UpdateEngine') {|bus| bus.start }
      respond_to do |accepts|
        accepts.html {render :text => "Start the update engine"}
        accepts.json {render :json => "Start the update engine"}
      end
    else
      respond_to do |accepts|
        accepts.html {render :text => "未初始化更新引擎", :status => :bad_request}
        accepts.json {render :json => "未初始化更新引擎", :status => :bad_request}
      end
    end
  end

  # 此服务用来通知level1节点有新的系统镜像。
  #  update: list of name of images => :ok
  def update_ue
    if call_bus('/cn/org/btc/UpdateEngine') { |bus|
        bus.download(params[:update_image_queue][:url],
                     params[:update_image_queue][:priority].to_i,
                     params[:update_image_queue][:size].to_i)
      }
      @req = UpdateImageQueue.new params[:update_image_queue]
      @req.progress = -1
      respond_to do |accept|
        accept.html
        accept.json
      end
    end
#
#     UpdateImageQueue.transaction do
#       was_req = UpdateImageQueue.find(:first,
#                                       :conditions => { :url => @req.url, :progress => -1},
#                                       :lock => true)
#       if was_req
#         @req.priority += was_req.priority
#         was_req.destroy
#       end
#       @req.save!
#     end
#     respond_to do |accept|
#       accept.html
#       accept.json
#     end
  end

  # 检索本地现有的镜像, 包括本地的和系统的。
  #  list_uiq  => a array of vdisks
  def list_ue
    @que = UpdateImageQueue.find(:all)
    respond_to do |accept|
      accept.html
      accept.json { render :json => @que}
    end
  end

end
