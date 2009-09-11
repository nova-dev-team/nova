require 'rubygems'
require 'rest_client'

class Pmachine < ActiveRecord::Base

  has_many :vmachines

  # start an vmachine on this pmachine
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
  end

end
