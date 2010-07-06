class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
    end
  end

  def self.down
    drop_table :vmachines
  end
end
