class CreateSettings < ActiveRecord::Migration
  def self.up
    create_table :settings do |t|
      t.column :key, :string, :limit => 40, :null => false
      t.column :value, :string, :null => false

      # "readonly" is used by ActiveRecord, so I have to use this name
      t.column :editable, :boolean, :default => true, :null => false
      t.timestamps
    end
  end

  def self.down
    drop_table :settings
  end
end
