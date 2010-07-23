# The model for storages' image_pool.
require 'fileutils'
require 'utils'
class VdiskPool < ActiveRecord::Base

# Intercepts savings for "vdisk_pool", and updates corresponding configs.
  def save
      basename_dir = File.join common_conf["storage_root"], "misc/pool_size", self.basename
      begin
        File.open(basename_dir + ".size", "w") do |f|
          f.write self.pool_size
        end
      rescue Exception => e
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
     basename_dir = File.join common_conf["storage_root"], "misc/pool_size", basename
     begin
        File.delete(basename_dir + ".size")
     rescue Exception => e
    end
  end
end
