require 'rubygems'
require 'rest_client'
require 'pp'
require 'json'

class Pmachine < ActiveRecord::Base

  has_many :vmachines

  # start an vmachine on this pmachine
  # TODO error handling
  def start_vm vm
    pmachine_addr = "http://#{self.ip}:#{self.port}"
    RestClient.post "#{pmachine_addr}/vmachines/start.json", {
      :arch => vm.arch,
      :name => "vm#{vm.id}",
      :vcpu => vm.cpu_count,
      :mem_size => vm.memory_size,
      :mac => vm.mac,
      :uuid => vm.uuid,
      :boot_dev => vm.boot_device,
      :hda => "vd1-sys-empty10g.qcow2",#(Vdisk.find_raw_name vm.hda),
      #:hdb => (Vdisk.find_raw_name vm.hdb),
      #:cdrom => (vm.cdrom)
    }
    self.vmachines << vm
    self.save
  end

  # do scheduling and start a vmachine
  # 1 return nil if no available pmachine found
  # 2 return the json return value from server
  def Pmachine.start_vm vm
    if Pmachine.count == 0 # no pmachine available
      return nil
    end

    pm_sched = Pmachine.first # TODO scheduling algorithm
    pm_sched.start_vm vm
  end

  def destroy_vm vm
    json_rest_request "http://#{self.ip}:#{self.port}/vmachines/destory.json", :uuid => vm.uuid
    self.vmachines.delete vm
  end

  def suspend_vm vm
    json_rest_request "http://#{self.ip}:#{self.port}/vmachines/suspend.json", :uuid => vm.uuid
  end

  def resume_vm vm
    json_rest_request "http://#{self.ip}:#{self.port}/vmachines/resume.json", :uuid => vm.uuid
  end

  ## return all pmachines that are not 'retired'
  def Pmachine.all_not_retired
    Pmachine.find_all_by_retired false
  end

  ## return all pmachines that are 'retired'
  def Pmachine.all_retired
    Pmachine.find_all_by_retired true
  end

  # send a request to this pmachine, and will also record 'health' condition of this pmachine
  def json_rest_request url, args
    begin
      result = RestClient.post url, args, :timeout => 1
    rescue
      # TODO
    end
    JSON.parse result
  end

  ## check if a pmachine is marked as 'retired'
  def retired?
    self.retired
  end

  ## retire pmachine
  def retire
    self.retired = true
    self.save
  end

  # undo retiring of pmachine
  def undo_retire
    self.retired = false
    self.save
  end

  ## register a new pmachine
  ## behave as a factory method
  def Pmachine.register
    # TODO
  end


  ## TODO check if a pmachine is working healthily
  def healthy?
    begin
      pp "requesting " + "http://#{self.ip}:#{self.port}/misc/hi.json"
      rest_request "http://#{self.ip}:#{self.port}/misc/hi.json", {}
      return true
    end
  end


end
