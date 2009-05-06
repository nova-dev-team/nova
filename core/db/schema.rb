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

ActiveRecord::Schema.define(:version => 20090417054023) do

  create_table "net_pools", :force => true do |t|
    t.string   "name",                                          :null => false
    t.string   "begin",                                         :null => false
    t.string   "mask",                                          :null => false
    t.integer  "size",         :limit => 11,                    :null => false
    t.boolean  "used",                       :default => false
    t.integer  "lock_version", :limit => 11, :default => 0
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "notifies", :force => true do |t|
    t.string   "notify_uuid"
    t.string   "notify_receiver_type"
    t.integer  "notify_receiver_id",   :limit => 11
    t.string   "notify_type"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "pmachines", :force => true do |t|
    t.string   "ip"
    t.string   "status",     :default => "working"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "users", :force => true do |t|
    t.string   "email"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vclusters", :force => true do |t|
    t.integer  "user_id",       :limit => 11
    t.string   "vcluster_name",               :default => "#unnamed#"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vimages", :force => true do |t|
    t.integer  "iid",        :limit => 11
    t.string   "os_family"
    t.string   "os_name"
    t.boolean  "hidden",                   :default => false
    t.string   "location"
    t.string   "comment"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vmachines", :force => true do |t|
    t.string   "ip"
    t.integer  "pmachine_id",        :limit => 11
    t.integer  "vcluster_id",        :limit => 11
    t.integer  "vimage_id",          :limit => 11
    t.string   "pmon_vmachine_uuid"
    t.string   "status",                           :default => "not running"
    t.string   "settings"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
