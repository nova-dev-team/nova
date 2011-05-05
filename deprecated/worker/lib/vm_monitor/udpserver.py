#coding=utf-8
import time
from socket import *
import commands
import os
import re
import threading
import codecs
from xml.dom import minidom
HOST = ''
PORT = 12345
BUFSIZE = 200000
ADDR = (HOST, PORT)
DataTab = {}
HistoryTab = {}
Alpha = 0.9
#http://www.kbao001.com/index.php?m=Goods&a=show&id=356&ru=13408
def ExecXentop():
  #print "xentop############"
  print "################################################"
  xd = ExecXm_list()
  cmd = "xentop -i 2 -b"
  str = commands.getoutput(cmd)
  cmd = "TERM=linux;export term;top -n 1 -b|head -n  4"
  #cmd = "TERM=linux;export TERM;xentop -i 1 -b"
  ss = commands.getoutput(cmd)
  #print ss
  #cmd="dmesg|grep "Brought"|awk'{print $4}'"
  str = str.replace('no limit', '-1')
  str = str.replace('n/a', '-1')
  #rx = 'CPUs: \d+ @'
  #cc = re.search(rx,ss)
  #CPUs=cc.group.split(' ')[1]
  #rx = '@ \d+'
  #cc=re.search(rx,ss)
  #MHz=cc.group.split(' ')[1]
  #print "cpus:"+CPUs,MHZ+":MHz"
  rx = '\d+k total'
  cc = re.search(rx, ss)
  #print ss
  totalmem = cc.group().split('k ')[0]
  rx = '\d+k used'
  cc = re.search(rx, ss)
  usedmem = cc.group().split('k ')[0]
  rx = '\d+k free'
  cc = re.search(rx, ss)
  freemem = cc.group().split('k ')[0]
    	#print totalmem,usedmem,freemem

    	#print 'cmd run success!'
  wordlinelist = str.split("\n")
  datadict = {}

  rownumber = len(wordlinelist)
  #dont hava table head
  lenx = len(xd) + 1
  # for shutdown or boot time
  wordlinelist = wordlinelist[rownumber - lenx:]
  #print rownumber
  #print xd
  #print wordlinelist
  dataname = []
  namelist = wordlinelist[0].split(" ")
  for dname in namelist:
    if(dname != " "and dname != ''):dataname.append(dname)
  #print dataname
  for wordline in wordlinelist[1:]:
        #print wordline
    domaindata = {}
    wordlist = wordline.split(" ")
    i = 0
    for word in wordlist:
      ss = word.strip()
      if(ss != "" and i < len(dataname)):
        domaindata[dataname[i]] = ss
        i = i + 1
    domaindata['Time'] = xd[domaindata[dataname[0]]]['Time(s)']
    domaindata['ID'] = xd[domaindata[dataname[0]]]['ID']
    domaindata['Mem'] = xd[domaindata[dataname[0]]]['Mem']
    domaindata['VCPUs'] = xd[domaindata[dataname[0]]]['VCPUs']
                # print ss
       		#print domaindata
    datadict[domaindata[dataname[0]]] = domaindata
  datadict['Domain-0']['phyMemtotal'] = totalmem
  datadict['Domain-0']['phyMemused'] = usedmem
  datadict['Domain-0']['phyMemfree'] = freemem

  return datadict
def ExecXm_list():
  cmd = "xm list"
  sx = commands.getoutput(cmd)
  wordlinelist = sx.split("\n")
  datadict = {}
  dataname = []
  namelist = wordlinelist[0].split(" ")
  for dname in namelist:
    if(dname != " " and dname != ""):dataname.append(dname)
  for wordline in wordlinelist[1:]:
    domaindata = {}
    wordlist = wordline.split(" ")
    i = 0
    for word in wordlist:
      ss = word.strip()
      if(ss != "" and i < len(dataname)):
        domaindata[dataname[i]] = ss
        i += 1
    datadict[domaindata[dataname[0]]] = domaindata
  #print datadict
  return datadict


def RecentRecords(records):
    global DataTab
    DataTab = {}
    #print len(records),"rrrrrrrrrrrrr",records
    for dc in records:
          for key in dc.keys():
              vv = dc[key]
  #continue
              if(DataTab.has_key(key)):
                 DataTab[key]['count'] += 1
                 DataTab[key]['totalmem'] += float(vv['MEM(%)'])
                 #print key
                 DataTab[key]['totalcpu'] += float(vv['CPU(%)'])
                 if float(vv['CPU(%)']) > DataTab[key]['maxcpu']:DataTab[key]['maxcpu'] = float(vv['CPU(%)'])

                 if(float(vv['MAXMEM(%)']) > DataTab[key]['maxmem']):DataTab[key]['maxmem'] = float(vv['MAXMEM(%)'])
                 DataTab[key]['NETTX'] += int(vv['NETTX(k)'])
                 DataTab[key]['NETRX'] += int(vv['NETRX(k)'])
                 DataTab[key]['VBD_RD'] += int(vv['VBD_RD'])
                 DataTab[key]['VBD_WR'] += int(vv['VBD_WR'])
                 DataTab[key]['ID'] = int(vv['ID'])
                 DataTab[key]['Mem'] = int(vv['Mem'])
                 DataTab[key]['VCPUs'] = int(vv['VCPUs'])
                 DataTab[key]['Time'] = float(vv['Time'])
              else:
                 nn = {}
                 nn['totalmem'] = float(vv['MEM(%)'])
                 nn['totalcpu'] = float(vv['CPU(%)'])
                 nn['avgmem'] = float(vv['MEM(%)'])
                 nn['avgcpu'] = float(vv['CPU(%)'])
                 nn['maxcpu'] = float(vv['CPU(%)'])
                 nn['maxmem'] = float(vv['MAXMEM(%)'])
                 nn['count'] = 1
                 nn['NETTX'] = int(vv['NETTX(k)'])
                 nn['NETRX'] = int(vv['NETRX(k)'])
                 nn['VBD_RD'] = int(vv['VBD_RD'])
                 nn['VBD_WR'] = int(vv['VBD_WR'])
                 nn['ID'] = int(vv['ID'])
                 nn['Mem'] = int(vv['Mem'])
                 nn['VCPUs'] = int(vv['VCPUs'])
                 nn['Time'] = float(vv['Time'])
                 DataTab[key] = nn
              if key == 'Domain-0':
                 #freemem=vv['freemem']
                 DataTab[key]['phyMemfree'] = int(vv['phyMemfree'])
                 DataTab[key]['phyMemtotal'] = int(vv['phyMemtotal'])
                 DataTab[key]['phyMemused'] = int(vv['phyMemused'])
                 DataTab[key]['phyCpus']=physinfo["Phy-CPUs"]
                 DataTab[key]['phyMhz']=physinfo["Phy-MHz"]
                 DataTab[key]['phyHDisk']=physinfo["phyHDisk"]
    #print "len:%d"%len(records)
    for key in dc.keys():
       DataTab[key]['avgmem'] = DataTab[key]['totalmem'] / DataTab[key]['count']
       DataTab[key]['avgcpu'] = DataTab[key]['totalcpu'] / DataTab[key]['count']
       DataTab[key]['NETTX'] = DataTab[key]['NETTX'] / DataTab[key]['count']
       DataTab[key]['NETRX'] = DataTab[key]['NETRX'] / DataTab[key]['count']
       DataTab[key]['VBD_RD'] = DataTab[key]['VBD_RD'] / DataTab[key]['count']
       DataTab[key]['VBD_WR'] = DataTab[key]['VBD_WR'] / DataTab[key]['count']

#连续调用 xentop ，持续监控一段时间内的性能数据,calculate the last 10 seconds data
def SuccessXentop(maxtime=1):
    count = 0
    #maxtime=10
    records = []
    while True:
       try:
          dc = ExecXentop()
       except KeyboardInterrupt:
          print "cccc"
          exit(0)
       except:
          #os.system("touch ff")
          continue
       count += 1
       records.append(dc)
       #time.sleep(1)
       g_mutex.acquire()
       RecentRecords(records)
       if count == maxtime:
          last = records.pop(0)
          count -= 1
                 #DataTab['Domain-0']=vv
       #print DataTab
              #print DataTab
       g_mutex.release()
#开始监控，使用xentop
def StartMonitor():
    #externip=cfgargs['externip']
    #t=threading.Thread(target=SuccessPing,args=(nodename,externip))
  #RunNetWorkLoad(cfgargs,vms,3)
    #t.start()
    f = threading.Thread(target=SuccessXentop)
    f.start()
    return f

def OutPut(FinalTab, rr, gg):
    context = ""
    line = ""
    for kk in gg:
        width = len(kk) + 5
        line += "%-*s" % (width, kk)
    context += line + "\n"
    line = "\n-----------------------------------------------------------------------------------------------------"
    #context+=line+"\n"
    #print FinalTab
    for i in range(0, len(rr)):
        record = FinalTab[rr[i]]
        line = ""
        for key in gg:
            width = len(key) + 5
            if record.has_key(key) == False:
         line += "None%*s" % (width - 4, "")
            elif isinstance(record[key], int):
                 line += "%-*d" % (width, record[key])
            elif isinstance(record[key], float):
                 line += "%-*.5f" % (width, record[key])
            elif isinstance(record[key], str):
                 line += "%-*s" % (width, record[key])
        context += line + '\n'
    #print context
    return context

def RemoveWordsFrom(words, wordsdict):
    for k1 in wordsdict.keys():
        #print "cc"
        for word in words:
            if wordsdict[k1].has_key(word):
               wordsdict[k1].pop(word)
    return wordsdict

def PrintReport():

  while(len(DataTab) == 0):time.sleep(1)
  vmtab = DataTab
  for dc in vmtab.keys():vmtab[dc]['vmname'] = dc
  context = ""
  #print vmtab
        words = []
  words.append("count")
  words.append("totalcpu")
  words.append("totalmem")
  vmtab = RemoveWordsFrom(words, vmtab)
  #context+="\nVM's Total Number=%d\n"%(vmnum)
        #context=context+"--------------------------Domain-0:\n"+str(vmtab['Domain-0'])+"\n"
  #vmtab.pop('Domain-0')
        #vmtab=RemoveWordsFrom(words,vmtab)
  rr = vmtab.keys()
  rr.sort()
  col = vmtab[rr[0]].keys()
  col.sort()
  context += OutPut(vmtab, rr, col)
  return context
  #context=context+"###One of THE  TESTS :\n"+str(dd)+"\n"

def Indent(dom, node, indent=0):
# Copy child list because it will change soon
  children = node.childNodes[:]
  if indent:
     text = dom.createTextNode('\n' + '\t' * indent)
     node.parentNode.insertBefore(text, node)
  if children:
     if children[-1].nodeType == node.ELEMENT_NODE:
        text = dom.createTextNode('\n' + '\t' * indent)
        node.appendChild(text)
  for n in children:
      if n.nodeType == node.ELEMENT_NODE:
         Indent(dom, n, indent + 1)

def ConstructXml(vmtab):
    xmldom = minidom.Document()
    print vmtab
    i = 0
    DomainInfo = xmldom.createElement('DomainInfo')
    xmldom.appendChild(DomainInfo)
    vmc = vmtab.keys()
    vmc.sort()
    for vmname in vmc:
        vmnode = xmldom.createElement('node')
        vmnode.setAttribute('vmname', vmname)
        i += 1
        vm = vmtab[vmname]
        vmnode.setAttribute('ID', str(vm['ID']))	
        vmnode.setAttribute('Mem', str(vm['Mem']))	
        vmnode.setAttribute('VCPUs', str(vm['VCPUs']))
        vmnode.setAttribute('Time', str(vm['Time']))		
        DomainInfo.appendChild(vmnode)
        print vm.keys()
        for name in vm.keys():
      if name in ['ID', 'vmname', 'Mem', 'VCPUs', 'count', 'Time', 'totalmem', 'totalcpu']:continue
      node = xmldom.createElement(name)
      print name,vm[name]
      textNode = xmldom.createTextNode(str(vm[name]))
      node.appendChild(textNode)
      print node
      vmnode.appendChild(node)
    Indent(xmldom, xmldom.documentElement)
    context = xmldom.toprettyxml()
    context = xmldom.toxml()
    print context
  #print len(context)
  #print "forecast:",ForeCast(vmtab)
    return context
def ForeCast(vmtab):
  curTab = vmtab
  #print vmtab
  global HistoryTab
  if len(HistoryTab) == 0:
    HistoryTab = vmtab
    return vmtab
  for vmname in vmtab.keys():
    vm = vmtab[vmname]
    for name in vm.keys():
      if name in ['vmname' , 'ID' , 'Time' , 'Mem', 'VCPUs', 'count', 'phyMemtotal']:continue
      #if(name=='vmname' or name=='ID' or name=='Time' Mem VCPUs
      curTab[vmname][name] = Alpha * vm[name] + (1 - Alpha) * HistoryTab[vmname][name]
      #print vmname,name,curTab[vmname][name]
  HistoryTab = curTab
  return curTab
def GetPhysinfo():
  cmd="xenmon.py -n -i 0"
  str=commands.getoutput(cmd)
  rx="Initialized with \d+"
  cc=re.search(rx,str)
  CPUs=cc.group().split(" ")[2]
  rx="Frequency = \d+.\d+"
  cc=re.search(rx,str)
  MHz=cc.group().split(" ")[2]
  print CPUs,MHz
  cmd="fdisk -l 2>/dev/null|grep \"Disk\"|awk '{ s+= $3}END{print s}'"
  str=commands.getoutput(cmd)
  HDisk=float(str)
  global physinfo
  physinfo={}
  physinfo["phyHDisk"]=HDisk
  physinfo["Phy-CPUs"]=int(CPUs)
  physinfo["Phy-MHz"]=float(MHz)

if __name__ == "__main__":
  GetPhysinfo()
  udpSerSock = socket(AF_INET, SOCK_DGRAM)
  udpSerSock.bind(ADDR)
  global g_mutex
  g_mutex = threading.Lock()
  trd = StartMonitor()
  #time.sleep(1)
  while len(DataTab) == 0:time.sleep(1)
  while True:
    #msg=PrintReport()
    g_mutex.acquire()
    vmtab = DataTab
    g_mutex.release()
    try:
      msg = ConstructXml(vmtab)
    #print "FFFForecast:######"
    #msg=ConstructXml(ForeCast(vmtab))
    #print ForeCast(DataTab)
    #print "waiting for message..."
    #time.sleep(1)
      data, addr = udpSerSock.recvfrom(BUFSIZE)
    except KeyboardInterrupt:
      print " exit!!...."
      udpSerSock.close()
      exit(0)

    #print '%s...received from:%s'%(data,addr)
    print msg, addr, time.ctime()
    udpSerSock.sendto(msg, addr)

  udpSerSock.close()


