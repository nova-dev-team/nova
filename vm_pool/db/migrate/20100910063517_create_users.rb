class CreateUsers < ActiveRecord::Migration
  def self.up
    # this table is used by liquid ftp
    create_table :users do |t|
      t.column :name,         :string, :null => false, :unique => true
      t.column :passwd,       :string, :null => false
      t.column :root_jail,    :string, :null => false
      t.column :group_id,     :integer, :default => -1
      t.timestamps
    end

    # those tables are not going to be used, just left here as dummy
    create_table :groups do |t|
      t.column :name,         :string, :null => false
      t.timestamps
    end

    create_table :user_rules do |t|
      t.column :user_id,      :integer, :null => false
      t.column :path,         :string, :null => false
      t.column :readable,     :boolean, :null => true
      t.column :writable,     :boolean, :null => false
      t.column :deletable,    :boolean, :null => false
      t.timestamps
    end

    create_table :group_rules do |t|
      t.column :group_id,     :integer, :null => false
      t.column :path,         :string, :null => false
      t.column :readable,     :boolean, :null => true
      t.column :writable,     :boolean, :null => false
      t.column :deletable,    :boolean, :null => false
      t.timestamps
    end
  end

  def self.down
    drop_table :users
    drop_table :groups
    drop_table :user_rules
    drop_table :group_rules
  end
end
