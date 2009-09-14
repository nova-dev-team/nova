require 'rubygems'
require 'rest_client'
require 'pp'
require 'json'
require 'timeout'

class Pmachine < ActiveRecord::Base

  has_many :vmachines

  # start an vmachine on this pmachine
  # TODO error handling
  def start_vm vm
    result = json_rest_request "vmachines/start", {
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

    # simple scheduling method, find pmachine with lowest load
    pm_sched = (Pmachine.all.sort {|pm1, pm2| pm1.vmachines.count <=> pm2.vmachines.count}).first
    pm_sched.start_vm vm
  end

  def destroy_vm vm
    json_rest_request "vmachines/destory", :uuid => vm.uuid
    self.vmachines.delete vm
  end

  def suspend_vm vm
    json_rest_request "vmachines/suspend", :uuid => vm.uuid
  end

  def resume_vm vm
    json_rest_request "vmachines/resume", :uuid => vm.uuid
  end

  ## register a new pmachine. if the pmachine is already registered, nothing will be changed
  ## behave as a factory method, and could be used as heartbeat
  def Pmachine.register params
    if (Pmachine.find_by_addr params[:addr]) == nil
      pm = Pmachine.new
      pm.addr = params[:addr]
      pm.vnc_first = params[:vnc_first]
      pm.vnc_last = params[:vnc_last]

      if pm.connected? # pm.connected? will not create premature database record. save it manually and safely
        pm.save
        puts "[Pmachine.register] Successfully registered new pmachine: #{pm.addr}."
        return true
      else
        puts "[Pmachine.register] Cannot connect to pmachine: #{pm.addr}."
        return false
      end
    else
      # already registered this pmachine, so we do nothing here, not even 'undo_retire' the pmachine!
      # this could be used as heartbeat message
      puts "[Pmachine.register] Pmachine already registered: #{params[:addr]}."
      return true
    end
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

  ## check if a pmachine is working correctly (connected)
  def connected?
    begin
      puts "[connected?] #{self.addr}"
      json_rest_request "misc/hi"
      mark_connected
      return true
    rescue => e
      puts e.message
      puts e.backtrace.join "\n"
      mark_unconnected
      return false
    end
  end


  ## find all pmachines that are not retired and connected
  def Pmachine.all_usable
    Pmachine.all_not_retired.select {|pm| pm.status == "connected"}
  end

  ## return all pmachines that are not 'retired'
  def Pmachine.all_not_retired
    Pmachine.find_all_by_retired false
  end

  ## return all pmachines that are 'retired'
  def Pmachine.all_retired
    Pmachine.find_all_by_retired true
  end

private

  def mark_unconnected
    self.status = "unconnected"
    self.save if self.id != nil # prevent prematurely creating new pmachine records in database, as in 'register' function
  end

  def mark_connected
    if self.status == "unconnected"
      # the pmachine was unconnected, on re-connection, sync settings and vm status to it
      update_settings
      update_vm_status
    end
    self.status = "connected"
    self.save if self.id != nil # prevent prematurely creating new pmachine records in database, as in 'register' function
  end

  # sync settings on master to pmachines
  def update_settings
    Setting.all_for_worker.each do |setting|
      json_rest_request "settings/edit", :key => setting.key, :value => setting.value
    end
  end

  # TODO make sure all vm on the pmachine has same status as in the master db
  def update_vm_status
  end

  # send a request to this pmachine, and will also record 'health' condition of this pmachine
  def json_rest_request method, args = nil
    begin
      method += ".json" unless method.end_with? ".json" # make sure this is a json call
      url = "http://#{self.addr}/#{method}"
      result = nil # forward declaration of the 'result' variable
      timeout(5) { result = args ? (RestClient.post url, args) : (RestClient.get url) }
      mark_connected
      JSON.parse result
    rescue => e
      puts e.message
      puts e.backtrace.join("\n")
      mark_unconnected
      raise e
    end
  end

end
