class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column :uuid,              :string, :limit => 40, :null => false
      t.column :cpu_count,         :integer, :default => 1
      t.column :memory_size,       :integer, :default => 256 # memory size in MB
      t.column :hda,               :string, :limit => 40
      t.column :hdb,               :string, :limit => 40
      t.column :cdrom,             :string, :limit => 40
      t.column :boot_device,       :string, :limit => 10, :default => "hd" # hd, cdrom
      t.column :arch,              :string, :limit => 10, :default => "i686" # x86_64, i686...
      t.column :ip,                :string, :limit => 20, :unique => true
      t.column :mac,               :string, :limit => 24
      t.column :hostname,          :string, :limit => 40
      t.column :vcluster_id,       :integer
      t.column :ceil_progress,     :integer, :default => -1
      t.column :last_ceil_message, :text
      t.column :vcluster_id,       :integer
      t.column :pmachine_id,       :integer
      t.column :destroyed,         :boolean, :default => false  # whether this vmachine has been destroyed
      t.column :status,            :string, :default => "not running" # could be "running", "not running", "suspended", "unknown". set to "unknown" when hosting pmachine is down

      t.column :vnc_port,          :integer
      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
