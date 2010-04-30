class CreateUsers < ActiveRecord::Migration
  def self.up
    create_table "users", :force => true do |t|
      t.column :login,                     :string, :limit => 20
      t.column :name,                      :string, :limit => 40, :default => '', :null => true
      t.column :email,                     :string, :limit => 40
      t.column :crypted_password,          :string, :limit => 40
      t.column :salt,                      :string, :limit => 40
      t.column :created_at,                :datetime
      t.column :updated_at,                :datetime
      t.column :remember_token,            :string, :limit => 40
      t.column :remember_token_expires_at, :datetime
      t.column :activated,                 :boolean, :default => false



      # Author::    Santa Zhang (santa1987@gmail.com)
      # Since::     0.3
      #
      # The user's privilege. Could be "root", "admin" or "normal user".
      t.column :privilege,                  :string, :limit => 20, :null => false

    end
    add_index :users, :login, :unique => true
  end

  def self.down
    drop_table "users"
  end
end
