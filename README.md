Overview
===================

Nova provides virtualized clusters atop a physical cluster so that it can be treated as a pool of computing resources. 

The system has several modules. A *master node* is in charge of handling requests, monitoring, scheduling, and also hosting a web UI. Each *worker node* runs a worker module, which manages virtual machines and reports their status to master. When a new VM boots, a pre-installed *agent* inside the VM fetches necessary information from outside and configures the new VM.

Nova strives to be mighty yet user-friendly. No client software is required since all operations can be done within a modern browser. Only a few clicks are required before a virtual cluster is up and ready to use. 

Prerequisite
============

Storage: NFS / FTP; 

Virtualization: [LXC](https://linuxcontainers.org/lxc/introduction/) / QEMU-KVM. 

Development
============

Current stable release is on branch `master`. 

Branch `sched-dev` is the cutting edge branch, which is under active development. 

Deployment
============

#### Test setup

Nova is tested on cutting edge hardwares and softwares. 

Tianyu's experiment setup (as of April 2016):

**Workstation:**  

```
Dell Optiplex 7040 Micro  
  |_ Core i5 6500T  
  |_ 4GB DDR4 SDRAM  
  |_ Samsung 850 series flash drive  
  |_ Fedora Workstation 23 x86_64 (Linux 4.4.6)  
  |_ Oracle Java SE 1.7.0_79  
```

**Server rack:**

Four worker nodes and a master node with the same configuration:

```
Dell PowerEdge R720  
  |_ Dual Xeon E5-2640 v2  
  |_ 32GB DDR3 ECC SDRAM  
  |_ CentOS 7.2 x86_64 (Linux 3.10.0)  
  |_ Oracle Java SE 1.7.0_79  
  |_ Libvirt 1.2.17  
```

#### Installation

TBD

Q & A
=======

TBA

#### Screenshot

![nova-instance](../../wiki/imgs/nova-instance.png)

Developers & maintainers
==========

#### Active contributors & maintainers

[Tianyu Chen](https://github.com/cty12), lead developer, current maintainer  
Dongbiao He, current developer and maintainer  

#### Previous contributors

Santa Zhang, santa1987@gmail.com, lead developer, main contributor  
HUANG Gang, herokuankuan@gmail.com, contributor  
ZHAO Xun, zhaoxun0920@126.com, contributor  
HOU Qinghua, houqh06@gmail.com, contributor  
GAO Tao, gaotao1987@gmail.com, contributor  
Feng Lin, frankvictor@qq.com, contributor  
XIN Jun, lead developer, contributor  

License
==========

TBA

