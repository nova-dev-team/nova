class CreateVimages < ActiveRecord::Migration
  def self.up
    create_table :vimages do |t|
      t.column "iid", :integer
      t.column "os_family", :string
      t.column "os_name", :string
      t.column "hidden", :boolean, :default => false
      t.column "location", :string
      t.column "comment", :string
      t.timestamps
    end
  end

  def self.down
    drop_table :vimages
  end
end
