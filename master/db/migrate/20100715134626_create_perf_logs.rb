class CreatePerfLogs < ActiveRecord::Migration
  def self.up
    create_table :perf_logs do |t|
      t.column :pmachine_id,  :integer
      t.column :time,         :string,   :limit => 14
      t.column :CPU,          :string,   :limit => 5
      t.column :memTotal,     :string,   :limit => 10
      t.column :memFree,      :string,   :limit => 10
      t.column :Rece,         :string,   :limit => 10
      t.column :Tran,         :string,   :limit => 10
      t.timestamps
    end
  end

  def self.down
    drop_table :perf_logs
  end
end
