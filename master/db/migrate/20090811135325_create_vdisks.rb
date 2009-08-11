class CreateVdisks < ActiveRecord::Migration
  def self.up
    create_table :vdisks do |t|

      t.timestamps
    end
  end

  def self.down
    drop_table :vdisks
  end
end
