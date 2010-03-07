class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column :name, :string, :null => false
      t.column :uuid, :string, :null => false
      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
