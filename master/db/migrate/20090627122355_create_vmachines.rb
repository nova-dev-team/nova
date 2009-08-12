class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column :uuid,     :string, :limit => 40, :null => false
      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
