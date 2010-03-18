class CreateSettings < ActiveRecord::Migration
  def self.up
    create_table :settings do |t|
      t.column :key,          :string, :limit => 40, :null => false
      t.column :value,        :string, :null => false

      # "readonly" is already used by rails
      t.column :editable,     :boolean, :default => true, :null => false

      t.timestamps
    end
  end

  def self.down
    drop_table :settings
  end
end
