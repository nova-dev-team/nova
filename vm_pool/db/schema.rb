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

ActiveRecord::Schema.define(:version => 20100910063517) do

  create_table "group_rules", :force => true do |t|
    t.integer  "group_id",   :null => false
    t.string   "path",       :null => false
    t.boolean  "readable"
    t.boolean  "writable",   :null => false
    t.boolean  "deletable",  :null => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "groups", :force => true do |t|
    t.string   "name",       :null => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "pmachines", :force => true do |t|
    t.string   "ip",           :limit => 20,                :null => false
    t.string   "hostname",     :limit => 40
    t.integer  "vm_pool_size",               :default => 4
    t.string   "status",                                    :null => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "settings", :force => true do |t|
    t.string   "key",        :limit => 40,                   :null => false
    t.string   "value",                                      :null => false
    t.boolean  "editable",                 :default => true, :null => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "user_rules", :force => true do |t|
    t.integer  "user_id",    :null => false
    t.string   "path",       :null => false
    t.boolean  "readable"
    t.boolean  "writable",   :null => false
    t.boolean  "deletable",  :null => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "users", :force => true do |t|
    t.string   "name",                       :null => false
    t.string   "passwd",                     :null => false
    t.string   "root_jail",                  :null => false
    t.integer  "group_id",   :default => -1
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vmachines", :force => true do |t|
    t.string   "name"
    t.string   "uuid",        :limit => 40,                    :null => false
    t.integer  "vnc_port"
    t.integer  "use_count",                 :default => 0,     :null => false
    t.boolean  "using",                     :default => false, :null => false
    t.integer  "pmachine_id"
    t.string   "status",      :limit => 40
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
