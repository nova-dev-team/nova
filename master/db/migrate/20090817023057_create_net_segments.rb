class CreateNetSegments < ActiveRecord::Migration
  def self.up
    create_table :net_segments do |t|
      t.column :head_ip, :string, :limit => 20  # eg. '10.0.10.1'
      t.column :size, :integer
      t.column :mask, :string, :limit => 20 # eg. '255.255.255.240'
      t.timestamps
    end
  end

  def self.down
    drop_table :net_segments
  end
end
