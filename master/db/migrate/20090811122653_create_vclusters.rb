class CreateVclusters < ActiveRecord::Migration
  def self.up
    create_table :vclusters do |t|
      t.column :cluster_name,                     :string
      t.column :package_list,                     :text

      # the owner's id
      t.column :user_id,                          :integer

      # TODO default vmachine settings
      t.column :cpu_count,      :integer
      t.column :memory_size,    :integer
      t.column :hda,            :string
      t.column :hdb,            :string
      t.column :cdrom,          :string
      t.column :boot_device,    :string
      t.column :arch,           :string
      t.column :net_segment_id, :integer

      t.column :destroyed,      :boolean, :default => false # whether this vcluster has been destroyed
      t.timestamps
    end
  end

  def self.down
    drop_table :vclusters
  end
end
