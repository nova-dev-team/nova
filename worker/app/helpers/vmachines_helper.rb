require 'libvirt'
require 'fileutils'
require 'pp'

module VmachinesHelper
  
  class Helper
  
    @@virt_conn = Libvirt::open("qemu:///system")

    def Helper.virt_conn
      @@virt_conn
    end

    def Helper.emit_xml_desc params

      if params[:cdrom] and params[:cdrom] != ""
        FileUtils.mkdir_p "#{params[:vmachines_root]}/#{params[:name]}" # assure path exists
        cdrom_desc = <<CDROM_DESC
    <disk type='file' device='cdrom'>
      <source file='#{params[:vmachines_root]}/#{params[:name]}/#{params[:cdrom]}'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
CDROM_DESC
      end
 
      if params[:hda] and params[:hda] != ""
        FileUtils.mkdir_p "#{params[:vmachines_root]}/#{params[:name]}" # assure path exists
        hda_desc = <<HDA_DESC
    <disk type='file' device='disk'>
      <source file='#{params[:vmachines_root]}/#{params[:name]}/#{params[:hda]}'/>
      <target dev='hda'/>
    </disk>
HDA_DESC
      end

      if params[:hdb] and params[:hdb] != ""
        FileUtils.mkdir_p "#{params[:vmachines_root]}/#{params[:name]}" # assure path exists
        hdb_desc = <<HDB_DESC
    <disk type='file' device='disk'>
      <source file='#{params[:vmachines_root]}/#{params[:name]}/#{params[:hdb]}'/>
      <target dev='hdb'/>
    </disk>
HDB_DESC
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
    <graphics type='vnc' port='#{params[:vnc_port]}'/>
  </devices>
</domain>
XML_DESC
    
      return xml_desc
    end

  end
end

