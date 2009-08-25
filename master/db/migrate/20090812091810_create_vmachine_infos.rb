class CreateVmachineInfos < ActiveRecord::Migration
  def self.up
    create_table :vmachine_infos do |t|
      t.column :vmachine_id,        :integer
      t.column :category,           :string, :limit => 20 # set max length to 20
      t.column :message,            :string
      t.timestamps
    end
    add_index :vmachine_infos, :vmachine_id, :unique => false
  end

  def self.down
    drop_table :vmachine_infos
  end
end


