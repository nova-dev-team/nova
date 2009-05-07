class CreateSoftwares < ActiveRecord::Migration
  def self.up
    create_table :softwares do |t|
      t.column "soft_name", :string
      t.timestamps
    end
  end

  def self.down
    drop_table :softwares
  end
end
