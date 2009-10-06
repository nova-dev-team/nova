class CreateSoftwareCategories < ActiveRecord::Migration
  def self.up
    create_table :software_categories do |t|
			
      t.timestamps
    end
  end

  def self.down
    drop_table :software_categories
  end
end
