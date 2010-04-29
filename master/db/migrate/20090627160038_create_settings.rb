class CreateSettings < ActiveRecord::Migration
  def self.up
    create_table :settings do |t|
      # Author::  Santa Zhang (santa1987@gmail.com)
      # Since::   0.3

      t.column :key,            :string, :limit => 40
      t.column :value,          :string

      # "readonly" is used by ActiveRecord, so I have to use this name instead.
      t.column :editable,       :boolean, :default => true, :null => false

      # Whether this setting should be forwarded to worker machines.
      t.column :for_worker,     :boolean, :default => false
      t.timestamps
    end
  end

  def self.down
    drop_table :settings
  end
end
