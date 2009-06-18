class CreatePmachines < ActiveRecord::Migration
  def self.up
    create_table :pmachines do |t|
      t.column "ip", :string
      t.column "status", :string, :default => "working" # could be "working", "pending remove"
      t.timestamps
    end
  end

  def self.down
    drop_table :pmachines
  end
end
