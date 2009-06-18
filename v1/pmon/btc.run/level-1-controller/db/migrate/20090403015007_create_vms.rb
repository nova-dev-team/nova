class CreateVms < ActiveRecord::Migration
  def self.up
    create_table :vms do |t|
      t.column :vm_uuid, :string, :null => false
      t.column :vm_status, :string, :null => false
      t.column :vm_def,  :text
      t.timestamps
    end
    add_index :vms, :vm_uuid,  :name => :vm_uuidi, :unique => true
  end

  def self.down
    drop_table :vms
  end
end
