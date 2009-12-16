#!/usr/bin/python

from ftplib import FTP

host = raw_input("host?\n")
port = int(raw_input("port?\n"))
connection_times = int(raw_input("connection times?\n"))
ls_times = int(raw_input("number of LIST cmd for each connection?\n"))

dir_counter = 0

for i in range(connection_times):
  try:
    ftp = FTP()
    ftp.connect(host, port)
    ftp.login('anonymous', 'santa')
    for j in range(ls_times):
      ftp.dir()
      dir_counter += 1
    ftp.quit();
  except:
    print "exception handled"

print "LIST comand served %d times" % dir_counter
