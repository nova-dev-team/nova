class CreateVdisks < ActiveRecord::Migration
  def self.up
    create_table :vdisks do |t|
      t.column :vd_kind, :string, :null => false
      t.column :vd_template, :string
      t.column :vd_status, :string
      t.column :vd_name, :string
      t.column :vd_uuid, :string, :null => false
      t.column :vm_uuid, :string
      t.column :lock_version, :integer, :default => 0
      t.timestamps
    end
  end

  def self.down
    drop_table :vdisks
  end
end
