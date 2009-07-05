Nova - Manage virtual machines running on a cluster
===================================================

Nova was designed to manage multiple virtual machines running on a cluster.
It includes a central controller called as "Core", and workers on other
machines called as "Pnode" (Physical node). It uses KFS as storage system.






Core component
==============


ActiveRecord Models
-------------------


Functions
---------


RESTful API
-----------





Pnode component
===============




Functions
---------


RESTful API
-----------



Storage system
==============


Content Addressable Storage
---------------------------




KFS storage switch
------------------


*** How to build kfs-0.3 ***

kfs-0.3's cmake script needs the following variables:

JAVA_INCLUDE_PATH
JAVA_INCLUDE_PATH2

Run the command with (you might need to change the path to Java include files):

cmake -DJAVA_INCLUDE_PATH=/usr/lib/jvm/java-sun-6/include -DJAVA_INCLUDE_PATH2=/usr/lib/jvm/java-6-sun/inlclude/linux ~/src/kfs/

NOTICE: when building kfs, xfs development files might be necessary, so you should install xfslib-dev

NFS storage switch
------------------

Corsair Carrier FS switch
-------------------------

Dependencies
============

+ libvirt
+ KFS-3.0 (if you want to use KFS storage switch)


Ruby on Rails
-------------


