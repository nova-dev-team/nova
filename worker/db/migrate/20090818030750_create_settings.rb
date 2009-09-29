class CreateSettings < ActiveRecord::Migration
  def self.up
    create_table :settings do |t|
      t.column :key, :string, :limit => 40
      t.column :value, :string
      t.column :editable, :boolean, :default => true # "readonly" is used by ActiveRecord, so I have to use this miserable name instead
      t.timestamps
    end
  end

  def self.down
    drop_table :settings
  end
end
