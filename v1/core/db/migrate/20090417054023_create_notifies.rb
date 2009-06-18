class CreateNotifies < ActiveRecord::Migration
  def self.up
    create_table :notifies do |t|
      t.column "notify_uuid", :string
      t.column "notify_receiver_type", :string # the one that should receive the notify
                                          # eg. the "vmachine"
      t.column "notify_receiver_id", :integer # the receiver's id
      t.column "notify_type", :string # such as 'deploying_finished', 'undeploying_finished'
      t.timestamps
    end
  end

  def self.down
    drop_table :notifies
  end
end
