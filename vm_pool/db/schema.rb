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

ActiveRecord::Schema.define(:version => 20100318080649) do

  create_table "pmachines", :force => true do |t|
    t.string   "ip",           :limit => 20,                :null => false
    t.string   "hostname",     :limit => 40
    t.integer  "vm_pool_size",               :default => 4
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

  create_table "vmachines", :force => true do |t|
    t.string   "uuid",        :limit => 40,                    :null => false
    t.integer  "use_count",                 :default => 0,     :null => false
    t.boolean  "using",                     :default => false, :null => false
    t.integer  "pmachine_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
