
module Utils
  require 'uuidtools'

  class IPCalc
    def initialize(ip, mask)
      @ip = ip.split('.').inject(0) {|total,value| (total << 8 ) + value.to_i}
      @mask = mask
    end

    def value
      return _int2ip
    end

    def next
      @ip += 1
      return _int2ip
    end

    def _int2ip()
      [24, 16, 8, 0].collect {|b| (@ip >> b) & 255}.join('.')
    end
  end

  def self.gen_mac(mac54, ip)
    mac54 + ip.split('.').inject { |p, x|
      p += ":" + ("%02x" % x.to_i)
    }
  end

  class KvmXml
    MAC54 = '00:FF'

    def initialize(args = {})
      @vm = {}
      @vm[:name] = 'yet_anothoer_vm' + rand(100).to_s
      @vm[:uuid] = UUID.random_create.to_s
      @vm[:image] = 'os100m.img'
      @vm[:ip] = '10.0.3.1'
      @vm[:mem] = '512'
      @vm[:vcpu] = 1

      args.each_pair do |k, v|
        @vm[k] = v
      end

      @vm[:mac] = Utils.gen_mac(MAC54, @vm[:ip])

    end

    def name
      @vm[:name]
    end

    def name=(name)
      @vm[:name] = name
    end

    def uuid
      @vm[:uuid]
    end

    def uuid=(uuid)
      @vm[:uuid] = uuid
    end

    def mac
      @vm[:mac]
    end

    def mac=(mac)
      @vm[:mac] = mac
    end

    def image
      @vm[:image]
    end

    def image=(image)
      @vm[:image] = image
    end

    def mem
      @vm[:mem]
    end

    def mem=(mem)
      @vm[:mem] = mem
    end

    def vcpu
      @vm[:vcpu]
    end

    def vcpu=(vcpu)
      @vm[:vcpu] = vcpu
    end

    def ip
      @vm[:ip]
    end

    def ip=(ip)
      @vm[:ip] = ip
    end

    def xml
xml = <<EOF
<domain type='kvm'>
  <name>
EOF

xml = xml.rstrip
srand Time.now.to_i
xml += @vm[:name]
xml += <<EOF
</name>
  <uuid>
EOF

xml = xml.rstrip
xml += @vm[:uuid]
xml += <<EOF
</uuid>
  <memory>512072</memory>
  <vcpu>1</vcpu>
  <os>
    <type arch='i686'>hvm</type>
  </os>
  <clock sync='localtime'/>
  <devices>
    <emulator>/usr/bin/kvm</emulator>
    <disk type='file' device='disk'>
      <source file='{:src=>"copy", :uuid=>"
EOF

xml = xml.rstrip
xml += @vm[:image]
xml += <<EOF
"}'/>
      <target dev='hda'/>
    </disk>
    <interface type='bridge'>
      <source bridge='br0'/>
      <mac address='
EOF

xml = xml.rstrip
xml += @vm[:mac]
xml += <<EOF
'/>
    </interface>
    <graphics type='vnc' port='-1'/>
  </devices>
</domain>
EOF

    end
  end
end
