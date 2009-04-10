module VclusterHelper

private

  # list all the vclusters
  def vcluster_list
    list = []
    Vcluster.all.each {|vcluster| list << "c#{vcluster.id}"}
    return list
  end

  # create a new cluster
  def vcluster_create
    result = {}
    vcluster = Vcluster.new

    if vcluster.save # if successfully saved
      result[:success] = true
      result[:msg] = "Created a new virtual cluster c#{vcluster.id}"
      result[:vcluster_cid] = "c#{vcluster.id}"

    else # failed to create the vcluster
      result[:success] = false
      result[:msg] = "Failed to create the new vcluster!"

    end

    return result
  end

  # delete an empty vcluster
  def vcluster_delete vcluster_cid
    result = {}
    vcluster = Vcluster.find_by_id vcluster_cid[1..-1] if vcluster_cid != nil

    if vcluster == nil # vcluster is not found
      result[:success] = false
      result[:msg] = "Vcluster #{vcluster_cid} not found!"

    else # vcluster found

      if vcluster.vmachines.empty? # could delete empty vcluster
        Vcluster.delete vcluster
        result[:success] = true
        result[:msg] = "Removed empty vcluster #{vcluster_cid}"

      else # cannot delete vcluster if it's not empty
        result[:success] = false
        result[:msg] = "Vcluster #{vcluster_cid} is not empty!"

      end
    end

    return result
  end


  # add a new virtual machine to the cluster
  def vcluster_add_vmachine vcluster_cid, vmachine_vid
    result = {}
    vcluster = Vcluster.find_by_id vcluster_cid[1..-1] if vcluster_cid != nil
    vmachine = Vmachine.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

    if vcluster == nil # vcluster is not found
      result[:success] = false
      result[:msg] = "Vcluster #{vcluster_cid} not found!"

    elsif vmachine == nil # vmachine not found
      result[:success] = false
      result[:msg] = "Vmachine #{vmachine_vid} not found!"

    else # both vcluster and vmachine are found
      if vmachine.vcluster != nil # vmachine is already added to some vcluster
        if vmachine.vcluster == vcluster # vmchine is already added to vclster
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is already added to vcluster #{vcluster_cid}"

        else # vmachine is already added to another vcluster
          result[:success] = false
          result[:msg] = "Vmachine #{vmachine_vid} is already added to another vcluster"

        end

      else # vmachine is not added to any cluster
        vcluster.vmachines << vmachine
        vmachine.save
        vcluster.save

        result[:success] = true
        result[:msg] = "Vmachine #{vmachine_vid} is added to vcluster #{vcluster_cid}"
      end
    end

    return result
  end

  # remove a vitural machine from a cluster. the vmachine must be "not running"
  def vcluster_remove_vmachine vcluster_cid, vmachine_vid
    result = {}
    vcluster = Vcluster.find_by_id vcluster_cid[1..-1] if vcluster_cid != nil

    if vcluster == nil # vcluster not found
      result[:success] = false
      result[:msg] = "Vcluster #{vcluster_cid} not found!"

    else # vcluster found
      vmachine = vcluster.vmachines.find_by_id vmachine_vid[1..-1] if vmachine_vid != nil

      if vmachine == nil # vmachine not found
        result[:success] = false
        result[:msg] = "Vcluster #{vcluster_cid} does not have vmachine #{vmachine_vid}!"

      elsif vmachine.status == "not running" # vmachine not running, could be removed from a vcluster
        vcluster.vmachines.delete vmachine
        vcluster.save
        vmachine.save

        result[:success] = true
        result[:msg] = "Removed vmachine #{vmachine_vid} from vcluster #{vcluster_cid}"

      else # vmachine is under use, cannot be removed from a vcluster
        result[:success]= false
        result[:msg] = "Vmachine #{vmachine_vid} is under use!"

      end
    end

    return result
  end

  # show the detailed information of a vcluster
  def vcluster_info vcluster_cid
    result = {}
    vcluster = Vcluster.find_by_id vcluster_cid[1..-1] if vcluster_cid != nil

    if vcluster == nil # vcluster not found
      result[:success] = false
      result[:msg] = "Vcluster #{vcluster_cid} not found!"

    else # vcluster found
      result[:success] = true
      result[:vcluster_cid] = "c#{vcluster.id}"

      vmachine_list = []
      vcluster.vmachines.each {|vmachine| vmachine_list << "v#{vmachine.id}"}
      result[:vmachines] = vmachine_list

    end
    return result
  end

end
