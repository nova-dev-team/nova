require 'libvirt'
require 'fileutils'

module VmachinesHelper

  class Helper

    def Helper.emit_xml_desc params

      # TODO figure out the local filename
      if params[:cdrom]
        FileUtils.mkdir_p "#{params[:vmachines_root]}/#{params[:name]}" # assure path exists
        cdrom_desc = <<CDROM_DESC
    <disk type='file' device='cdrom'>
      <source file='#{params[:vmachines_root]}/#{params[:name]}/#{params[:cdrom]}'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
CDROM_DESC
      end
 
      # TODO figure out the disk filename
      if params[:hda]
        FileUtils.mkdir_p "#{params[:vmachines_root]}/#{params[:name]}" # assure path exists
        hda_desc = <<HDA_DESC
    <disk type='file' device='disk'>
      <source file='#{params[:vmachines_root]}/#{params[:name]}/#{params[:hda]}'/>
      <target dev='hda'/>
    </disk>
HDA_DESC
      end

# TODO figure out what should be filled into the xml file
# grpahics type=vnc port=-1: -1 means the system will automatically allocate an port for vnc
      xml_desc = <<XML_DESC
<domain type='qemu'>
  <name>#{params[:name]}</name>
  <uuid>#{params[:uuid]}</uuid>
  <memory>#{params[:mem_size] * 1024}</memory>
  <vcpu>#{params[:vcpu]}</vcpu>
  <os>
    <type arch='i686' machine='pc'>hvm</type>
    <boot dev='cdrom' />
  </os>
  <devices>
    <emulator>/usr/bin/kvm</emulator>
#{hda_desc if params[:hda]}
#{cdrom_desc if params[:cdrom]}
    <graphics type='vnc' port='-1'/>
  </devices>
</domain>
XML_DESC
    
      return xml_desc
    end

  end
end
