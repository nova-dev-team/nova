#!/usr/bin/ruby

require 'rubygems'
require 'libvirt'
require 'pp'

require '../scripts/nodelist.rb' # for sys_exec function

sys_exec "bash stop.sh"

virt_conn = Libvirt::open("qemu:///system")

virt_conn.list_defined_domains.each do |domain_name|
  puts "Destroying domain '#{domain_name}'"
  begin
    dom = virt_conn.lookup_domain_by_name domain_name
    puts "UUID=#{dom.uuid}"
    begin
      dom.destroy
    rescue
      # do nothing here
    end
    dom.undefine
  rescue
    next
  end
end

sys_exec "rm tmp/work_site -rf"

sys_exec "bash first_run.sh"
sys_exec "bash start.sh"

