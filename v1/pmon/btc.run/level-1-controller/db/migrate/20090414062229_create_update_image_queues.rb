class CreateUpdateImageQueues < ActiveRecord::Migration
  def self.up
    create_table :update_image_queues do |t|
      t.column :url, :string, :null => false
      t.column :size, :integer
      t.column :priority, :integer, :default => 10
      t.column :progress, :integer, :default => -1
      t.column :lock_version, :integer, :default => 0
      t.timestamps
    end
  end

  def self.down
    drop_table :update_image_queues
  end
end
