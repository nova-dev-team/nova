class CreateSettings < ActiveRecord::Migration
  def self.up
    create_table :settings do |t|
      t.column :key, :string, :limit => 40
      t.column :value, :string
      t.column :no_edit, :boolean, :default => false # "readonly" is used by ActiveRecord, so I have to user this miserable name
      t.timestamps
    end
  end

  def self.down
    drop_table :settings
  end
end
