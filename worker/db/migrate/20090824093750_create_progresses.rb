class CreateProgresses < ActiveRecord::Migration
  def self.up
    create_table :progresses do |t|
      t.column :owner, :string # vm1, vm2...etc
      t.column :info, :string # detail infomation about the job
      t.column :status, :string # eg, pending, in action...

      t.timestamps
    end
  end

  def self.down
    drop_table :progresses
  end
end
