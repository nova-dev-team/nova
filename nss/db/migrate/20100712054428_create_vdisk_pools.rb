class CreateVdiskPools < ActiveRecord::Migration
  def self.up
    create_table :vdisk_pools do |t|
      t.column :basename,       :string, :limit => 40, :null => false
      t.column :pool_size,      :int, :null => false

      t.column :info,      :string

      t.column :editable,     :boolean, :default => true, :null =>false

      t.timestamps
    end
  end

  def self.down
    drop_table :vdisk_pools
  end
end
