class CreatePmachines < ActiveRecord::Migration
  def self.up
    create_table :pmachines do |t|
      t.column :ip, :string, :limit => 20
      t.column :port, :integer, :default => 3000  # default port is 3000 for pmachine servers
      t.column :vnc_first, :integer # the first usable vnc port
      t.column :vnc_last, :integer   # the last usable vnc port
      t.timestamps
    end
  end

  def self.down
    drop_table :pmachines
  end
end
