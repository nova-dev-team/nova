class CreatePmachines < ActiveRecord::Migration
  def self.up
    create_table :pmachines do |t|

      t.timestamps
    end
  end

  def self.down
    drop_table :pmachines
  end
end
