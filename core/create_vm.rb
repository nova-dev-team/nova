#!/usr/bin/ruby

require 'rubygems'
require 'uuidtools'
require 'optparse'
require 'ostruct'
require 'pp'
#require 'REXML/Document'


$VERSION = 'version 2, support 5btc'


def send_post(url, xml)
  require 'uri'
  require 'net/http'

  u = URI.parse(url)
  req = Net::HTTP::Post.new(u.path)
  req.set_form_data({:define => xml })
  res = Net::HTTP.new(u.host, u.port).start { |http| http.request(req) }
  case res
  when Net::HTTPSuccess, Net::HTTPRedirection
    puts 'ok'
  else
    res.error!
  end
end


class OptparseExample

  CODES = %w[iso-2022-jp shift_jis euc-jp utf8 binary]
  CODE_ALIASES = { "jis" => "iso-2022-jp", "sjis" => "shift_jis" }

  #
  # Return a structure describing the options.
  #
  def self.parse(args)
    options = OpenStruct.new
    options.verbose = false
    options.name = 'yet_anothoer_vm' + rand(100).to_s
    options.uuid = UUID.random_create.to_s
    options.mac = "00:FF:0A:00:03:01"
    options.image = "intrepid2.img"
    options.server = "10.0.0.210"
    options.port = "3000"


    opts = OptionParser.new do |opts|
      opts.banner = "Usage: create_vm.rb [options]"

      opts.separator ""
      opts.separator "Specific options:"

      # Mandatory argument.
      opts.on("-n", "--name NAME",
                "虚拟机的名字，不能与现有的虚拟机重复。") do |name|
          options.name = name
      end

      opts.on("-u", "--uuid UUID",
                "虚拟机的UUID，不能与现有的虚拟机重复。") do |uuid|
          options.uuid = uuid
      end

      opts.on("-m", "--mac MACADDR",
                "虚拟机网卡的MAC地址。") do |mac|
          options.mac = mac
      end

      opts.on("-i", "--image IMAGE",
                "虚拟机的系统影像UUID。") do |image|
          options.image = image
      end

      opts.on("-p", "--server IP",
                "服务器的IP。") do |ip|
          options.server = ip
      end

      opts.on("-o", "--port PORT",
                "服务器的端口。") do |port|
          options.server = port
      end

      # Boolean switch.
      opts.on("-v", "--[no-]verbose", "Run verbosely") do |v|
        options.verbose = v
      end

      opts.separator ""
      opts.separator "Common options:"

      # No argument, shows at tail.  This will print an options summary.
      # Try it and see!
      opts.on_tail("-h", "--help", "Show this message") do
        puts opts
        exit
      end

      # Another typical switch to print the version.
      opts.on_tail("--version", "Show version") do
        puts $VERSION
        exit
      end
    end

    opts.parse!(args)
    options
  end  # parse()

end  # class OptparseExample

srand Time.now.to_i

options = OptparseExample.parse(ARGV)

xml = <<EOF
<domain type='kvm'>
  <name>
EOF

xml = xml.rstrip
srand Time.now.to_i
xml += options.name
xml += <<EOF
</name>
  <uuid>
EOF

xml = xml.rstrip
xml += options.uuid
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
xml += options.image
xml += <<EOF
"}'/>
      <target dev='hda'/>
    </disk>
    <interface type='bridge'>
      <source bridge='br0'/>
      <mac address='
EOF

xml = xml.rstrip
xml += options.mac
xml += <<EOF
'/>
    </interface>
    <graphics type='vnc' port='-1'/>
  </devices>
</domain>
EOF

if options.verbose
#  REXML::Document.new(xml).write $stdout
  pp xml
end

url = 'http://' + options.server + ':' + options.port + '/x/create'
send_post url, xml

