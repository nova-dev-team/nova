class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|

      # Author::    Santa Zhang
      # Since::     0.3
      t.column :name,               :string
      t.column :uuid,               :string, :limit => 40, :null => false
      t.column :cpu_count,          :integer, :default => 1
      t.column :soft_list,          :string, :default => ""

      # Unit of memory size is MB
      t.column :memory_size,        :integer, :default => 256
      t.column :hda,                :string, :limit => 40

      t.column :cdrom,              :string, :limit => 40

      # Which device will the machine be booted.
      # It could be "hd", or "cdrom"
      t.column :boot_device,        :string, :limit => 10, :default => "hd"

      # The architecture of the VM.
      # Could be "i686" or "x86_64", etc.
      t.column :arch,               :string, :limit => 10, :default => "i686"

      t.column :ip,                 :string, :limit => 20
      t.column :vcluster_id,        :integer
      t.column :pmachine_id,        :integer

      # The status of the vmachine.
      # Could be:
      #   * shut-off: the machine is not running
      #   * start-pending: the machine is waiting to be started
      #   * start-preparing: the machine is preparing for resources
      #   * suspended: the machine is suspended
      #   * running: the machine is up and running
      #   * shutdown-pending: the machine is waiting to be shut off
      #   * connect-failure: failed to connect to the hosting pmachine
      #   * boot-failure: failed to start the vmachine
      #
      # Since::     0.3
      t.column :status,             :string, :default => "shut-off"

      # The VNC port for the VM.
      #
      # Since::     0.3
      t.column :vnc_port,           :integer

      # The hypervisor to be used, could be "xen", "kvm", etc
      #
      # Since::     0.3
      t.column :hypervisor,         :string

      # Migration info: the IP of physical machines.
      #
      # Since::     0.3
      t.column :migrate_from,       :string
      t.column :migrate_to,         :string

      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end

