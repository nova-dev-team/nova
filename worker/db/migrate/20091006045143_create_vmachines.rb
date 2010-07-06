class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|

      # The hypervisor to be used.
      #
      # Since::     0.3
      t.column :hypervisor,           :string

      # Migration info: the IP of physical machines.
      #
      # Since::     0.3
      t.column :migrate_from,         :string
      t.column :migrate_to,           :string
      
      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
