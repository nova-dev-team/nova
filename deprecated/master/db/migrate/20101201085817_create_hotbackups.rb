class CreateHotbackups < ActiveRecord::Migration
  def self.up
    create_table :hotbackups do |t|
      t.column :vmachine_id, :integer
      t.column :from_ip, :string
      t.column :to_ip, :string
      t.timestamps
    end
  end

  def self.down
    drop_table :hotbackups
  end
end
