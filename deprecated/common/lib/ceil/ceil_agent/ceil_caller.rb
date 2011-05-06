require 'socket'

j = TCPSocket::new('localhost', 32167)
j.write("12345678")
str = j.recv(100)
puts str

j.write("addjob gundam")
j.write("ftp://miao:miao@59.66.124.241/pp")
str = j.recv(100)
puts str

j.close

