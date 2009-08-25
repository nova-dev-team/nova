class CreateVclusters < ActiveRecord::Migration
  def self.up
    create_table :vclusters do |t|
      t.column :cluster_name,                     :string
      t.column :package_list,                     :text
      t.timestamps
    end
  end

  def self.down
    drop_table :vclusters
  end
end
