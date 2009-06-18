require 'dbus'
require File.join("../../shared-lib/", 'tools')

class UpdateEngineBus < DBus::Object
  include Tools

  def join_to(daemon)
    @daemon = daemon
  end

  # Create an interface aggregating all upcoming dbus_method defines.
  dbus_interface "cn.org.btc.UpdateEngine" do
    dbus_method :start do
      LOGGER.debug "uebus.start ==> "
      @daemon.start_sched if @daemon
      @ue_started
    end

    dbus_method :stop do
      LOGGER.debug "uebus.stop ==> "
      @daemon.stop_sched if @daemon
      @ue_stoped
    end

    dbus_method :check, "out stat:s" do
      LOGGER.debug "uebus.check ==> "
      stat = @daemon.check_sched if @daemon
      LOGGER.debug "uebus.check  <== #{stat}"
      [stat.to_s]
    end

    dbus_method :download, "in url:s, in priority:u, in size:u" do |url, priority, size|
      LOGGER.debug "uebus.download ==> #{url}, #{priority}, #{size}"
      @daemon.request_download(url, priority, size) if @daemon
    end

    dbus_signal :download_progress, "uuid:s, progress:u"
    dbus_signal :ue_started
    dbus_signal :ue_stoped
  end
end

def create_ue_bus
  bus = DBus::SystemBus.instance
  service = bus.request_service("cn.org.btc.UpdateEngine")
  ueintf = UpdateEngineBus.new("/cn/org/btc/UpdateEngine")

  service.export(ueintf)

  yield ueintf

  main = DBus::Main.new
  main << bus
  main.run
end


