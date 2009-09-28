class CreateSoftwares < ActiveRecord::Migration
  def self.up
    create_table :softwares do |t|
			t.column :software_name, 						:string, :limit => 20
			t.column :description,							:text
			t.column :available,  							:boolean

			t.column :software_category_id, 		:integer


      t.timestamps
    end
  end

  def self.down
    drop_table :softwares
  end
end
