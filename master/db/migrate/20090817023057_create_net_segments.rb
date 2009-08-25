class CreateNetSegments < ActiveRecord::Migration
  def self.up
    create_table :net_segments do |t|
      t.column :name, :string, :limit => 20  # eg. 'nova-02'
      t.column :head_ip, :string, :limit => 20  # eg. '10.0.10.1'
      t.column :size, :integer
      t.column :mask, :string, :limit => 20 # eg. '255.255.255.240'
      t.column :vcluster_id, :integer
      t.column :used, :boolean, :default => false
      t.column :lock_version, :integer, :default => 0
      t.timestamps
        
    end
  end

  def self.down
    drop_table :net_segments
  end
end
