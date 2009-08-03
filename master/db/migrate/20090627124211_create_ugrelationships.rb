class CreateUgrelationships < ActiveRecord::Migration
  def self.up
    create_table :ugrelationships do |t|
      t.column :user_id, :integer
      t.column :group_id, :integer

      t.timestamps
    end
  end

  def self.down
    drop_table :ugrelationships
  end
end
