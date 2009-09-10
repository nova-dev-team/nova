require 'libvirt'
require 'fileutils'
require 'uri'
require 'pp'
require 'pretty_file_size'

module VmachinesHelper
  
  class Helper

    include VdiskNaming
    include Util
  
    @@virt_conn = Libvirt::open("qemu:///system")

    def Helper.virt_conn
      @@virt_conn
    end

    def Helper.emit_xml_desc params

      if params[:cdrom] and params[:cdrom] != ""
        FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
        cdrom_desc = <<CDROM_DESC
    <disk type='file' device='cdrom'>
      <source file='#{Setting.vmachines_root}/#{params[:name]}/#{params[:cdrom]}'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
CDROM_DESC
      end
 
      if params[:hda] and params[:hda] != ""
        FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
        if VdiskNaming::vdisk_type(params[:hda]).start_with? "system"
          real_hda_filename = "vd-notsaved-#{params[:uuid]}-hda.qcow2"
        else
          real_hda_filename = params[:hda]
        end
        hda_desc = <<HDA_DESC
    <disk type='file' device='disk'>
      <source file='#{Setting.vmachines_root}/#{params[:name]}/#{real_hda_filename}'/>
      <target dev='hda'/>
    </disk>
HDA_DESC
      end

      if params[:hdb] and params[:hdb] != ""
        FileUtils.mkdir_p "#{Setting.vmachines_root}/#{params[:name]}" # assure path exists
        if VdiskNaming::vdisk_type(params[:hdb]).start_with? "system"
          real_hdb_filename = "vd-notsaved-hdb.qcow2"
        else
          real_hdb_filename = params[:hdb]
        end
        hdb_desc = <<HDB_DESC
    <disk type='file' device='disk'>
      <source file='#{Setting.vmachines_root}/#{params[:name]}/#{real_hdb_filename}'/>
      <target dev='hdb'/>
    </disk>
HDB_DESC
      end

      if params[:mac] and params[:mac] != ""
        mac_desc = <<MAC_DESC
    <interface type='network'>
      <source network='default'/>
      <mac address='#{params[:mac]}'/>
    </interface>
MAC_DESC
      end

# grpahics type=vnc port=-1: -1 means the system will automatically allocate an port for vnc
      xml_desc = <<XML_DESC
<domain type='qemu'>
  <name>#{params[:name]}</name>
  <uuid>#{params[:uuid]}</uuid>
  <memory>#{params[:mem_size].to_i * 1024}</memory>
  <vcpu>#{params[:vcpu]}</vcpu>
  <os>
    <type arch='#{params[:arch]}' machine='pc'>hvm</type>
    <boot dev='#{params[:boot_dev]}' />
  </os>
  <devices>
    <emulator>/usr/bin/kvm</emulator>
#{hda_desc if params[:hda] and params[:hda] != ""}
#{hdb_desc if params[:hdb] and params[:hdb] != ""}
#{cdrom_desc if params[:cdrom] and params[:cdrom] != ""}
#{mac_desc if params[:mac] and params[:mac] != ""}
    <graphics type='vnc' port='#{params[:vnc_port]}' listen='0.0.0.0'/>
    <input type='tablet' bus='usb' />
  </devices>
</domain>
XML_DESC
    
      return xml_desc
    end

    def Helper.cached_storage_server_filelist
      # showing cached results
      lines = []
      begin
        File.open(Setting.resource_list_cache, "r") do |file|
          lines = file.readlines
        end
      rescue
        # do nothing
      end
      return lines
    end

    # used by vmachines worker's "update"
    def Helper.list_files dir_uri
      scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split dir_uri  # parse URI information
    
      files_list = []
      if scheme == "file"
        Dir.new(path).entries.each do |entry|
          files_list << entry
        end
      elsif scheme == "ftp"
        username, password = Util::split_userinfo userinfo
        Net::FTP.open(host, username, password) do |ftp|
          files_list = ftp.list("*")
        end
      else
        raise "Resource scheme '#{scheme}' not known!"
      end
      return files_list
    end

  end
end

