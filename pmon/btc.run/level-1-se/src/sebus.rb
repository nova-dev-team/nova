
require 'dbus'
require File.join("../../shared-lib/", 'tools')

class StorageEngineBus < DBus::Object
  include Tools

  def join_to(daemon)
    @daemon = daemon
  end

  # Create an interface aggregating all upcoming dbus_method defines.
  dbus_interface "cn.org.btc.StorageEngine" do
    dbus_method :start do
      LOGGER.debug "sebus.start  => "
      @daemon.start_sched if @daemon
      se_started
    end

    dbus_method :stop do
      LOGGER.debug "sebus.stop  => "
      @daemon.stop_sched if @daemon
      se_stoped
    end

    dbus_method :check, "out stat:s" do
      LOGGER.debug "sebus.check  => "
      stat = @daemon.check_sched if @daemon
      LOGGER.debug "sebus.check  <== #{stat}"
      [stat.to_s]
    end

    dbus_method :create, "in uuid:s, in size:u, in format:s" do
      LOGGER.debug "sebus.create a vdisk => #{uuid}, #{size}, #{format}"
      @daemon.request_create if @daemon
    end

    dbus_method :delete, "in vm_uuid:s" do |vm_uuid|
      LOGGER.debug "sebus.delete the vdisk => #{vm_uuid}"
      @daemon.request_del_vdisk(vm_uuid) if @daemon
    end

    dbus_method :get, "in vd_uuid:s, in vm_uuid:s, out uuid:s" do |vd_uuid, vm_uuid|
      LOGGER.debug "sebus.get a vdisk #{vd_uuid} for #{vm_uuid}"
      uuid = @daemon.request_get_vdisk(vd_uuid, vm_uuid) if @daemon
      LOGGER.debug "sebus.get <== #{[uuid]}"
      [uuid]
    end

    dbus_signal :start_clone, "uuid:s"
    dbus_signal :start_create, "uuid:s"
    dbus_signal :start_delete, "uuid:s"
    dbus_signal :finish_clone, "uuid:s"
    dbus_signal :finish_create, "uuid:s"
    dbus_signal :finish_delete, "uuid:s"
    dbus_signal :clone_progress, "uuid:s, progress:u"
    dbus_signal :create_progress, "uuid:s, progress:u"
    dbus_signal :se_started
    dbus_signal :se_stoped
  end
end

def create_se_bus()
  bus = DBus::SystemBus.instance
  service = bus.request_service("cn.org.btc.StorageEngine")
  seintf = StorageEngineBus.new("/cn/org/btc/StorageEngine")

  service.export(seintf)

  yield seintf

  main = DBus::Main.new
  main << bus
  main.run
end


