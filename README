About Nova platform
===================
Nova provides virtualized clusters on top of physical clusters. Its design is like Amazon EC2.
It has several modules. A "master" node is in charge of request handling, system monitoring,
VM scheduling, and also hosts a portal for users. Each worker machine (physical machine hosting
virtual machines) runs a "worker" module, which manages virtual machines, and reports health
status to master. When a new VM boots, a pre-installed agent inside the VM will fetch necessary
info from the outside, and configures the new VM. The agent is capable of change network settings
and installing software, etc.

Nova strives to give users a very friendly interface. No client software is required, since all
interaction is done inside a browser with Flash support. Users only need to do a few clicks, and
a virtual cluster will be up and running in a few minutes.


Prerequisite
============
KVM+FTP mode:
* The installer only works under Ubuntu Server.
* Needs Internet connection for "apt-get install".

XEN+NFS mode:
* Only works in CentOS 5.4 or above.
* Needs Internet connection for "yum install".


Installation
============
See "tools/installer/README".


Developers
==========
v0.3:
Santa Zhang, santa1987@gmail.com (Lead developer)
HUANG Gang, herokuankuan@gmail.com
ZHAO Xun, zhaoxun0920@126.com
HOU Qinghua, houqh06@gmail.com
GAO tao, gaotao1987@gmail.com
Feng lin, frankvictor@qq.com

v0.2:
Santa Zhang, santa1987@gmail.com (Lead developer)
HUANG Gang, herokuankuan@gmail.com

v0.1:
XIN Jun (Lead developer)
Santa Zhang, santa1987@gmail.com
HUANG Gang, herokuankuan@gmail.com

