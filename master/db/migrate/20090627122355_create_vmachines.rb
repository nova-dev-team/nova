class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column :uuid,              :string, :limit => 40, :null => false
      t.column :cpu_count,         :integer, :default => 1
      t.column :memory_size,       :integer, :default => 256 # memory size in MB
      t.column :hda,               :integer # id of hda vdisk
      t.column :hdb,               :integer # id of hdb vdisk
      t.column :cdrom,             :integer # id of cdrom vdisk
      t.column :boot_device,       :string, :limit => 10 # hda, cdrom
      t.column :architecture,      :string, :limit => 10 # x86_64, i686...
      t.column :ip,                :string, :limit => 20, :unique => true
      t.column :mac,               :string, :limit => 24
      t.column :hostname,          :string, :limit => 40
      t.column :vcluster_id,       :integer
      t.column :ceil_progress,     :integer, :default => -1
      t.column :last_ceil_message, :text
      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
