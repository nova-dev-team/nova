from socket import *
import sys
import time
import xml.sax.handler
import pprint
class DomainHandler(xml.sax.handler.ContentHandler):
  def __init__(self):
    self.innode=0
    self.mapping={}
    self.vminfo={}
  def startElement(self,name,attributes):
    if name=="node":
      self.buffer=""
      self.vminfo={}
      for kk in attributes.keys():
        self.vminfo[kk]=attributes[kk]
      #self.vmname=attributes["vmname"]
      #self.id=attributes["ID"]
  def characters(self,data):
    self.buffer=data
    #print data
  def endElement(self,name):
    if name=="DomainInfo":pass
    elif name=="node":
      self.mapping[self.vminfo['vmname']]=self.vminfo
    else:
      self.vminfo[name]=self.buffer

def Communicate(ip,Interval,flag):
  HOST=ip
  PORT=12345
  BUFSIZE=200000
  ADDR=(HOST,PORT)
  #Interval=1
  Client=socket(AF_INET,SOCK_DGRAM)
  if(flag==1):
    Client=socket(AF_INET,SOCK_STREAM)
    Client.connect(ADDR)

  while True:
    #data=raw_input('> ')
    data=time.ctime()
    time.sleep(Interval)
    #print data
    if(flag==1):
      Client.send(data)
      data=Client.recv(BUFSIZE)
    elif flag==0:
      Client.sendto(data,ADDR)
      data=Client.recvfrom(BUFSIZE)								
    if not data:
      break
    print data[0];break

  Client.close()
def ResolveXml(msg):
  #print msg
  #dom=minidom.parseString(msg)

  #parser=xml.sax.make_parser()
  handler=DomainHandler()
  #parser.setContentHandler(handler)
  #parser.parseString(msg)
  xml.sax.parseString(msg,handler)
  #pprint.pprint(handler.mapping)
  dd=handler.mapping
  #print dd
  return dd
if __name__=='__main__':

  if len(sys.argv)<2:
    print "please specific a serveraIP!"
    exit(1)
  ip=sys.argv[1]
  interval=1
  if len(sys.argv)>2:
    inetrval=float(sys.argv[2])
  socktype=0
  if len(sys.argv)>3:
    socktype=sys.argv[3]
  Communicate(ip,interval,socktype)
