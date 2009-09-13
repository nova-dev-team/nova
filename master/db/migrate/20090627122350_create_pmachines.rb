class CreatePmachines < ActiveRecord::Migration
  def self.up
    create_table :pmachines do |t|
      t.column :ip, :string, :limit => 20 # TODO deprecate this column
      t.column :port, :integer, :default => 3000  # default port is 3000 for pmachine servers # TODO deprecate this column
      t.column :addr, :string # "ip:port" # TODO use this column instead of ip & column
      t.column :vnc_first, :integer # the first usable vnc port
      t.column :vnc_last, :integer   # the last usable vnc port
      t.column :health, :string, :default => "healthy" # whether the pmachine is connected

      t.column :retired, :boolean, :default => false # 'retire' mark, whether the new vms should be allocated to this pmachine
      t.timestamps
    end
  end

  def self.down
    drop_table :pmachines
  end
end
