# This file is auto-generated from the current state of the database. Instead of editing this file, 
# please use the migrations feature of Active Record to incrementally modify your database, and
# then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your database schema. If you need
# to create the application database on another system, you should be using db:schema:load, not running
# all the migrations from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20100510120715) do

  create_table "pmachines", :force => true do |t|
    t.string   "ip",          :limit => 20,                :null => false
    t.string   "status",                                   :null => false
    t.string   "hostname"
    t.integer  "vm_capacity",               :default => 2
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "settings", :force => true do |t|
    t.string   "key",        :limit => 40
    t.string   "value"
    t.boolean  "editable",                 :default => true,  :null => false
    t.boolean  "for_worker",               :default => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "softwares", :force => true do |t|
    t.string   "file_name",    :limit => 100, :null => false
    t.string   "display_name",                :null => false
    t.string   "description"
    t.string   "os_family"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "users", :force => true do |t|
    t.string   "login",                     :limit => 20
    t.string   "name",                      :limit => 40, :default => ""
    t.string   "email",                     :limit => 40
    t.string   "crypted_password",          :limit => 40
    t.string   "salt",                      :limit => 40
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "remember_token",            :limit => 40
    t.datetime "remember_token_expires_at"
    t.boolean  "activated",                               :default => false
    t.string   "privilege",                 :limit => 20,                    :null => false
  end

  add_index "users", ["login"], :name => "index_users_on_login", :unique => true

  create_table "vclusters", :force => true do |t|
    t.string   "cluster_name"
    t.string   "first_ip"
    t.integer  "cluster_size"
    t.integer  "user_id"
    t.string   "ssh_public_key"
    t.string   "ssh_private_key"
    t.string   "os_username"
    t.string   "os_passwd"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vdisks", :force => true do |t|
    t.string   "file_name",    :limit => 100
    t.string   "display_name"
    t.string   "description"
    t.string   "disk_format"
    t.string   "os_family"
    t.string   "os_name"
    t.string   "soft_list"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vmachine_infos", :force => true do |t|
    t.integer  "vmachine_id"
    t.string   "category"
    t.string   "message"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "vmachine_infos", ["vmachine_id"], :name => "index_vmachine_infos_on_vmachine_id"

  create_table "vmachines", :force => true do |t|
    t.string   "name"
    t.string   "uuid",        :limit => 40,                         :null => false
    t.integer  "cpu_count",                 :default => 1
    t.string   "soft_list",                 :default => ""
    t.integer  "memory_size",               :default => 256
    t.string   "hda",         :limit => 40
    t.string   "cdrom",       :limit => 40
    t.string   "boot_device", :limit => 10, :default => "hd"
    t.string   "arch",        :limit => 10, :default => "i686"
    t.string   "ip",          :limit => 20
    t.integer  "vcluster_id"
    t.integer  "pmachine_id"
    t.string   "status",                    :default => "shut-off"
    t.integer  "vnc_port"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
