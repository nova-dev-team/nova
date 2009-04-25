class CreateVmachines < ActiveRecord::Migration
  def self.up
    create_table :vmachines do |t|
      t.column "ip", :string
      t.column "pmachine_id", :integer
      t.column "vcluster_id", :integer
      t.column "vimage_id", :integer # discarded
      t.column "pmon_vmachine_uuid", :string # the uuid for pmon
      t.column "status", :string, :default => "not running"
        # other possible values for "status":
        # deploying: when copying data to pmachine, and installing software
        # running: when the vmachine is running and serving to user
        # suspended: when the vmachine is suspended by the user
        # undeploying: when copying data back to data center
        
      t.column "settings", :string # settings in json
        # included:
        # cpu count, memory size, disks size, disk images' uuids, etc

      t.timestamps
    end
  end

  def self.down
    drop_table :vmachines
  end
end
