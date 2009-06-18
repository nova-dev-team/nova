class CreateVclusters < ActiveRecord::Migration
  def self.up
    create_table :vclusters do |t|
      t.column "user_id", :integer
      t.column "vcluster_name", :string, :default => "#unnamed#"   # the name of the cluster
      t.column "net_pool_name", :string, :default => ""
      t.timestamps
    end
  end

  def self.down
    drop_table :vclusters
  end
end
