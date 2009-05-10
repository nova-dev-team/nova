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

ActiveRecord::Schema.define(:version => 20090414062229) do

  create_table "requests", :force => true do |t|
    t.string   "kind",       :null => false
    t.string   "uuid",       :null => false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "requests", ["kind", "uuid"], :name => "reqi", :unique => true

  create_table "update_image_queues", :force => true do |t|
    t.string   "url",                          :null => false
    t.integer  "size"
    t.integer  "priority",     :default => 10
    t.integer  "progress"
    t.integer  "lock_version", :default => 0
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vdisks", :force => true do |t|
    t.string   "vd_kind",                     :null => false
    t.string   "vd_template"
    t.string   "vd_status"
    t.string   "vd_name"
    t.string   "vd_uuid",                     :null => false
    t.string   "vm_uuid"
    t.integer  "lock_version", :default => 0
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "vms", :force => true do |t|
    t.string   "vm_uuid",    :null => false
    t.string   "vm_status",  :null => false
    t.text     "vm_def"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "vms", ["vm_uuid"], :name => "vm_uuidi", :unique => true

end
