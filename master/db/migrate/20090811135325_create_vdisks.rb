class CreateVdisks < ActiveRecord::Migration
  def self.up
    create_table :vdisks do |t|
      t.column :raw_name, :string, :limit => 100
      t.column :display_name, :string
      t.column :description, :string
      t.column :type, :string
      t.column :base_upon, :integer
      t.column :software_category_id, :integer

      t.timestamps
    end
  end

  def self.down
    drop_table :vdisks
  end
end
