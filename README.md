About Nova platform
===================

Nova provides virtualized clusters atop a physical cluster so that it can be treated as a pool of computing resources. 

The system has several modules. A *master node* is in charge of handling requests, monitoring, scheduling, and also hosting a web UI. Each *worker node* runs a worker module, which manages virtual machines and reports their status to master. When a new VM boots, a pre-installed *agent* inside the VM fetches necessary information from outside and configures the new VM.

Nova strives to be mighty yet user-friendly. No client software is required since all operations can be done within a modern browser. Only a few clicks are required before a virtual cluster is up and ready to use. 


Prerequisite
============

Storage: NFS / FTP; 

Virtualization: LXC / QEMU-KVM. 

Development
============

Current stable release is on branch `master`. 

Branch `sched-dev` is the cutting edge branch, which is under active development. 

Deployment
============

TBD

Developers & maintainers
==========

#### Active contributors

[Tianyu Chen](https://github.com/cty12), lead developer, current maintainer  
Dongbiao He, current developer and maintainer  

#### Previous contributors

Santa Zhang, santa1987@gmail.com (Lead developer)  
HUANG Gang, herokuankuan@gmail.com  
ZHAO Xun, zhaoxun0920@126.com  
HOU Qinghua, houqh06@gmail.com  
GAO tao, gaotao1987@gmail.com  
Feng lin, frankvictor@qq.com  

Santa Zhang, santa1987@gmail.com (Lead developer)  
HUANG Gang, herokuankuan@gmail.com  

XIN Jun (Lead developer)  
Santa Zhang, santa1987@gmail.com  
HUANG Gang, herokuankuan@gmail.com  

License
==========

TBA

