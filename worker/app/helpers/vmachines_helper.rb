module VmachinesHelper

  class Helper

    def Helper.emit_xml_spec params
      return <<XML_SPEC
<domain type='qemu'>
  <name>#{params["name"]}</name>
  <uuid>#{params["uuid"]}</uuid>
  <memory>#{params["mem_size"] * 1024}</memory>
  <vcpu>#{params["vcpu"]}</vcpu>
  <os>
    <type arch='i686' machine='pc'>hvm</type>
    <boot dev='cdrom' />
  </os>
  <devices>
    <emulator>/usr/bin/kvm</emulator>
    <disk type='file' device='cdrom'>
      <source file='/home/santa/Downloads/liveandroidv0.2.iso'/>
      <target dev='hdc'/>
      <readonly/>
    </disk>
    <disk type='file' device='disk'>
      <source file='/home/santa/Downloads/vdisk.img'/>
      <target dev='hda'/>
    </disk>
    <graphics type='vnc' port='-1'/>
  </devices>
</domain>
XML_SPEC
    end

  end

end
