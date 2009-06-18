class CreateRequests < ActiveRecord::Migration
  def self.up
    create_table :requests do |t|
      t.column :kind, :string, :null => false
      t.column :uuid, :string, :null => false
      t.timestamps
    end
    add_index :requests, [:kind, :uuid], :name => "reqi", :unique => true
  end

  def self.down
    drop_table :requests
  end
end
