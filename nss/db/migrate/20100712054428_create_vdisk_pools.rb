class CreateVdiskPools < ActiveRecord::Migration
  def self.up
    create_table :vdisk_pools do |t|

      t.timestamps
    end
  end

  def self.down
    drop_table :vdisk_pools
  end
end
