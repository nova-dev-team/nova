
module Utils
  require 'rubygems'
  require 'uuidtools'

  class IPCalc

    def self.int2ip(ip)
      [24, 16, 8, 0].collect {|b| (ip >> b) & 255}.join('.')
    end

    def self.gen_mac(ip, mac54='54:7E')
      mac54 + ':' + ip.split('.').inject { |p, x|
        p += ":" + ("%02x" % x.to_i)
      }
    end

    def initialize(ip, mask, mac54='54:7E')
      @ip = ip.split('.').inject(0) {|total,value| (total << 8 ) + value.to_i}
      @mask = mask
      @mac54 = mac54
    end

    def value
      return(IPCalc.int2ip(@ip))
    end

    def mask
      IPCalc.int2ip(0b11111111111111111111111111111111 << (32 - @mask))
    end

    def next
      @ip += 1
      return IPCalc.int2ip(@ip)
    end

    def mac
      IPCalc.gen_mac value(), @mac54
    end
  end

  class KvmXml

    def initialize(args = {})
      @vm = {}
      @vm[:name] = 'yet_anothoer_vm' + rand(100).to_s
      @vm[:uuid] = UUID.random_create.to_s
      @vm[:image] = 'os100m.img'
      @vm[:ip] = '10.0.3.1'
      @vm[:mem] = 512000
      @vm[:vcpu] = 1
      @vm[:bridge] = 'br0'

      args.each_pair do |k, v|
        @vm[k] = v
      end

      @vm[:mac] = Utils::IPCalc.gen_mac(@vm[:ip])

    end

    def bridge
      @vm[:bridge]
    end

    def bridge=(bridge)
      @vm[:bridge] = bridge
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
      @vm[:mem] = mem * 1024
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
  <memory>
EOF

xml = xml.rstrip
xml += @vm[:mem].to_s
xml += <<EOF
</memory>
  <vcpu>
EOF

xml = xml.rstrip
xml += @vm[:vcpu].to_s
xml += <<EOF
</vcpu>
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
      <source bridge='
EOF
xml = xml.rstrip
xml += @vm[:bridge]
xml += <<EOF
'/>
      <mac address='
EOF

xml = xml.rstrip
xml += @vm[:mac]
xml += <<EOF
'/>
    </interface>
    <graphics type='vnc' port='-1' listen='0.0.0.0'/>
  </devices>
</domain>
EOF

    end
  end


end
