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

ActiveRecord::Schema.define(:version => 20090817023057) do

  create_table "groups", :force => true do |t|
    t.string   "name",       :limit => 40
    t.boolean  "special",                  :default => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "net_segments", :force => true do |t|
    t.string   "name",         :limit => 20
    t.string   "head_ip",      :limit => 20
    t.integer  "size"
    t.string   "mask",         :limit => 20
    t.integer  "vcluster_id"
    t.boolean  "used",                       :default => false
    t.integer  "lock_version",               :default => 0
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "pmachines", :force => true do |t|
    t.string   "ip",         :limit => 20
    t.integer  "port",                     :default => 3000
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "settings", :force => true do |t|
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "ugrelationships", :force => true do |t|
    t.integer  "user_id"
    t.integer  "group_id"
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
  end

  add_index "users", ["login"], :name => "index_users_on_login", :unique => true

  create_table "vclusters", :force => true do |t|
    t.string   "cluster_name"
    t.text     "package_list"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vdisks", :force => true do |t|
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vmachine_infos", :force => true do |t|
    t.integer  "vmachine_id"
    t.string   "category",    :limit => 20
    t.integer  "status",                    :default => -1
    t.string   "message"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "vmachine_infos", ["vmachine_id"], :name => "index_vmachine_infos_on_vmachine_id"

  create_table "vmachines", :force => true do |t|
    t.string   "uuid",              :limit => 40,                 :null => false
    t.string   "ip",                :limit => 20
    t.string   "mac",               :limit => 24
    t.string   "hostname",          :limit => 40
    t.integer  "vcluster_id"
    t.integer  "ceil_progress",                   :default => -1
    t.text     "last_ceil_message"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
