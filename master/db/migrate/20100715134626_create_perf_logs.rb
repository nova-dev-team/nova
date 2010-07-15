class CreatePerfLogs < ActiveRecord::Migration
  def self.up
    create_table :perf_logs do |t|
      t.column :pmachine_id, :integer
      t.timestamps
    end
  end

  def self.down
    drop_table :perf_logs
  end
end
