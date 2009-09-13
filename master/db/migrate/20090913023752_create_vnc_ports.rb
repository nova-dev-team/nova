class CreateVncPorts < ActiveRecord::Migration
  def self.up
    create_table :vnc_ports do |t|
      t.column :port, :integer
      t.column :lock_versino, :integer
      t.timestamps
    end
  end

  def self.down
    drop_table :vnc_ports
  end
end
