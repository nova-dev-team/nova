class CreateSoftwareCategories < ActiveRecord::Migration
  def self.up
    create_table :software_categories do |t|
			t.column :software_category_name,	:string, :limit => 40
      t.timestamps
    end
  end

  def self.down
    drop_table :software_categories
  end
end
