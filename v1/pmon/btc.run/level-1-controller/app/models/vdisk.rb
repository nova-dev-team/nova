# == Schema Information
# Schema version: 20090403015007
#
# Table name: vdisks
#
#  id           :integer         not null, primary key
#  vd_kind      :string(255)     not null
#  vd_template  :string(255)
#  vd_status    :string(255)
#  vd_name      :string(255)
#  vd_uuid      :string(255)     not null
#  vm_uuid      :string(255)
#  lock_version :integer         default(0)
#  created_at   :datetime
#  updated_at   :datetime
#

class Vdisk < ActiveRecord::Base
  SYS_ROOT = File.join RAILS_ROOT, 'tmp/system'
  LOCAL_ROOT = File.join RAILS_ROOT, 'tmp/local'

  def filename
    if vd_kind == 'system'
      return File.join(SYS_ROOT, vd_uuid)
    else
      return File.join(LOCAL_ROOT, vd_uuid)
    end
  end

  def template_filename
    return File.join(SYS_ROOT, vd_template) unless vd_template == nil
  end
end
