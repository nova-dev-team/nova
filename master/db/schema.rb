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

ActiveRecord::Schema.define(:version => 20090911061436) do

  create_table "bdrb_job_queues", :force => true do |t|
    t.text     "args"
    t.string   "worker_name"
    t.string   "worker_method"
    t.string   "job_key"
    t.integer  "taken"
    t.integer  "finished"
    t.integer  "timeout"
    t.integer  "priority"
    t.datetime "submitted_at"
    t.datetime "started_at"
    t.datetime "finished_at"
    t.datetime "archived_at"
    t.string   "tag"
    t.string   "submitter_info"
    t.string   "runner_info"
    t.string   "worker_key"
    t.datetime "scheduled_at"
  end

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
    t.integer  "vnc_first"
    t.integer  "vnc_last"
    t.string   "health",                   :default => "healthy"
    t.boolean  "retired",                  :default => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "settings", :force => true do |t|
    t.string   "key",        :limit => 40
    t.string   "value"
    t.boolean  "no_edit",                  :default => false
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
    t.integer  "user_id"
    t.integer  "cpu_count"
    t.integer  "memory_size"
    t.string   "hda"
    t.string   "hdb"
    t.string   "cdrom"
    t.string   "boot_device"
    t.string   "arch"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vdisks", :force => true do |t|
    t.string   "raw_name",     :limit => 100
    t.string   "display_name"
    t.string   "description"
    t.string   "type"
    t.integer  "base_upon"
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
    t.string   "uuid",              :limit => 40,                     :null => false
    t.integer  "cpu_count",                       :default => 1
    t.integer  "memory_size",                     :default => 256
    t.string   "hda",               :limit => 40
    t.string   "hdb",               :limit => 40
    t.string   "cdrom",             :limit => 40
    t.string   "boot_device",       :limit => 10
    t.string   "arch",              :limit => 10, :default => "i686"
    t.string   "ip",                :limit => 20
    t.string   "mac",               :limit => 24
    t.string   "hostname",          :limit => 40
    t.integer  "vcluster_id"
    t.integer  "ceil_progress",                   :default => -1
    t.text     "last_ceil_message"
    t.integer  "pmachine_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
