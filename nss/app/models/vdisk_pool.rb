# The model for storages' image_pool.
require 'fileutils'

class VdiskPool < ActiveRecord::Base

# Intercepts savings for "vdisk_pool", and updates corresponding configs.
  def save
      File.open("#{RAILS_ROOT}/../../misc/pool_size/" + self.basename + ".size", "w") do |f|
        f.write self.pool_size
      end
    super
  end

# Add rows into VdiskPool table.
  def VdiskPool.add (basename, pool_size)
    rows = VdiskPool.new
    rows.basename = basename
    rows.pool_size = pool_size
    rows.save
  end

# Change size of specified template in the VdiskPool table.
  def VdiskPool.csize (basename, pool_size)
    rows = VdiskPool.find(:first, :conditions => ["basename = ?",basename])
    rows.pool_size = pool_size
    rows.save
  end

# Delete the specified rows from VdiskPool table.
  def VdiskPool.del (basename)
     VdiskPool.delete_all(["basename = ?", basename])
     File.delete("#{RAILS_ROOT}/../../misc/pool_size/" + basename + ".size")
  end
end
