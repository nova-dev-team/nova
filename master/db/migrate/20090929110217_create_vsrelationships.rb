class CreateVsrelationships < ActiveRecord::Migration
  def self.up
    create_table :vsrelationships do |t|
      t.column :software_category_id, :integer
      t.column :vdisk_id, :integer
      t.timestamps
    end
  end

  def self.down
    drop_table :vsrelationships
  end
end
