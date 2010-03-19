class CreatePmachines < ActiveRecord::Migration
  def self.up
    create_table :pmachines do |t|
      # the ip address of the pmachine, it is required
      t.column :ip,             :string, :limit => 20, :null => false

      # the host name of the pmachine, it will be automatically collected by daemons
      t.column :hostname,       :string, :limit => 40

      # the number of VMs constantly running on the pmachine
      t.column :vm_pool_size,   :integer, :default => 4

      # the status of the physical machine.
      # could be "pending" (connecting), "working", "failure", "retired"
      t.column :status,         :string, :null => false

      t.timestamps
    end
  end

  def self.down
    drop_table :pmachines
  end
end
