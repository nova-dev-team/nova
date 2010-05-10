class CreateSoftwares < ActiveRecord::Migration
  def self.up
    create_table :softwares do |t|
      
      # Author::    Santa Zhang
      # Since::     0.3

      # The real file name on storage server.
      t.column :file_name,        :string, :limit => 100, :null => false

      # The name that will be seen by users.
      t.column :display_name,     :string, :null => false

      # Description for the software. Coule be null.
      t.column :description,      :string

      # The os family that best matchs the software. Could be null.
      t.column :os_family,        :string


      t.timestamps
    end
  end

  def self.down
    drop_table :softwares
  end
end
