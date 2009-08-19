require 'rubygems'
require 'libvirt'

   conn = Libvirt::open("qemu:///system")
   puts conn.capabilities
   
   #File::open("domain.xml") do |f| 
   #  conn.createDomainLinux(f.readlines, nil)
   #end

 #  dom = conn.lookupDomainByName("mydomain")
  # dom.suspend
   #dom.resume
   #puts dom.xmlDesc

    l = []
    list_of_vm = []
    conn.list_defined_domains.each {|id|
      l << conn.lookup_domain_by_name(id) }
    l.each do |d|
      h = {}
      h[:name]  = d.name
      h[:uuid]  = d.uuid
      h[:state] = "#{ d.id} / #{d.info.state}"
      h[:cpu_time] = d.info.cpu_time
      list_of_vm << h
    end

    l = []
    conn.list_domains.each {|id|
      l << conn.lookup_domain_by_id(id) }
    l.each do |d|
      h = {}
      h[:name]  = d.name
      h[:uuid]  = d.uuid
      h[:state] = "#{ d.id} / #{d.info.state}"
      h[:cpu_time] = d.info.cpu_time
      list_of_vm << h
    end

puts "finally!!!"
puts list_of_vm


