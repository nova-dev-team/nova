class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column :uuid,           :string, :limit => 40, :null => false
      
      # How many times has the VM been used?
      t.column :use_count,      :integer, :default => 0, :null => false
      
      # Is the VM being used now?
      t.column :using,          :boolean, :default => false, :null => false
      t.column :pmachine_id,    :integer

      # The observed status of the VM (by poller)
      t.column :status,         :string, :limit => 40

      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
