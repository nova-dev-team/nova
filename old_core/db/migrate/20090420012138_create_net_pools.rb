class CreateNetPools < ActiveRecord::Migration
  def self.up
    create_table :net_pools do |t|
      t.column :name, :string, :null => false
      t.column :begin, :string, :null => false
      t.column :mask, :string, :null => false
      t.column :size, :integer, :null => false
      t.column :used, :bool, :default => false
      t.column :lock_version, :integer, :default => 0
      t.timestamps
    end
  end

  def self.down
    drop_table :net_pools
  end
end
