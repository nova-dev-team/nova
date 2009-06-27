class CreateGroups < ActiveRecord::Migration
  def self.up
    create_table :groups do |t|
      t.column :name,       :string, :limit => 40

      t.timestamps
    end
  end

  def self.down
    drop_table :groups
  end
end
