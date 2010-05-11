class CreateVclusters < ActiveRecord::Migration
  def self.up
    create_table :vclusters do |t|
      # Author::    Santa Zhang
      
      t.column :cluster_name,                     :string

      # The first IP allocated to this cluster.
      # The VM's IP are determined according to this value.
      #
      # Since::     0.3
      t.column :first_ip,                         :string

      # Maximum size of this cluster. It is the limit of vmachines in this cluster.
      #
      # Since::     0.3
      t.column :cluster_size,                     :integer

      # The owner's id
      #
      # Since::     0.3
      t.column :user_id,                          :integer
      
      t.timestamps
    end
  end

  def self.down
    drop_table :vclusters
  end
end
