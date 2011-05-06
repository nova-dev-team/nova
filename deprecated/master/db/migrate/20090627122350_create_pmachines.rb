class CreatePmachines < ActiveRecord::Migration
  def self.up
    create_table :pmachines do |t|

      # The IP address of the physical machine
      t.column :ip,             :string, :limit => 20, :null => false
      
      # The status of the physical machine.
      # Possible values are "pending", "working", "failure", "retired".
      t.column :status,         :string, :null => false

      # The host name of physical machine.
      t.column :hostname,       :string

      # The uuid of the worker machine
      t.column :uuid,           :string

      # The MAC address of the worker machine, used for remote booting
      t.column :mac_address,    :string

      # The limit of running VMs on this machine.
      # It is not a hard limit, but creating VMs more than this limit will result in low performance.
      t.column :vm_capacity,    :integer, :default => 2

      t.timestamps
    end
  end

  def self.down
    drop_table :pmachines
  end
end
