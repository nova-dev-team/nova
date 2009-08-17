class CreateNetSegments < ActiveRecord::Migration
  def self.up
    create_table :net_segments do |t|

      t.timestamps
    end
  end

  def self.down
    drop_table :net_segments
  end
end
