require 'libvirt'

module VmachinesHelper

  class Helper

    def Helper.emit_xml_desc params
      cdrom_desc = <<CDROM_DESC
    <disk type='file' device='cdrom'>
      <source file='#{params[:storage_server]}/#{params[:cdrom]}'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
CDROM_DESC
  
      hda_desc = <<HDA_DESC
    <disk type='file' device='disk'>
      <source file='/home/santa/Downloads/vdisk.img'/>
      <target dev='hda'/>
    </disk>
HDA_DESC

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
#{cdrom_desc if params[:cdrom]}
#{hda_desc if params[:hda]}
    <graphics type='vnc' port='-1'/>
  </devices>
</domain>
XML_DESC
    
      return xml_desc
    end

  end
end
