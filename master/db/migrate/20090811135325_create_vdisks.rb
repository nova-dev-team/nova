class CreateVdisks < ActiveRecord::Migration
  def self.up
    create_table :vdisks do |t|

      # Author::      Santa Zhang
      # Since::       0.3

      # The name on storage server.
      t.column :file_name,        :string, :limit => 100

      # The name that will be seen by users.
      t.column :display_name,     :string

      # The description for this disk image.
      t.column :description,      :string

      # Type (format) of this image.
      # Could be "qcow2", "iso".
      t.column :type,             :string

      # The kind of operation system of this image.
      # Could be "windows", "linux".
      # If the image has nothing to do with operation system (such as a .iso data cd), this value should be set to null.
      t.column :os_family,        :string, :null => true, :default => nil

      # The precise name of the operation system, with version included.
      # Here is some suggested names:
      #   "Windows XP", "Windows XP (SP3)", "Windows Vista", "Windows 7";
      #   "Ubuntu 8.04", "Ubuntu 10.04".
      #
      # If the image has nothing to do with operation system (such as a .iso data cd), this value should be set to null.
      t.column :os_name,          :string, :null => true, :default => nil

      t.timestamps
    end
  end

  def self.down
    drop_table :vdisks
  end
end
