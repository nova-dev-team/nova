class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column :uuid,           :string, :limit => 40, :null => false
      t.column :use_count,      :integer, :default => 0, :null => false
      t.column :using,          :boolean, :default => false, :null => false
      t.column :pmachine_id,    :integer

      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
