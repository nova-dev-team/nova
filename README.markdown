STATUS: STILL UNDER (ACTIVE) DEVELOPMENT!
=========================================
Currently, Nova (version 2) still cannot run with full function, only parts of it is working.
I am (very) actively working on this project, and a milestone was intended to come out around September this year.


Nova - Manage virtual machines running on a cluster
===================================================

Nova was designed to manage multiple virtual machines running on a cluster.
It includes a central controller called as "Core", and workers on other
machines called as "Pnode" (Physical node). It uses KFS as storage system.



How to get it up and running?
=============================
Follow the instructions in 'INSTALL' document.


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



Storage component
=================


Philosophy: Content Addressable Storage
---------------------------------------


KFS storage
-----------

*** How to build kfs-0.3 with out help from the Rakefile? ***

kfs-0.3's cmake script needs the following variables:

    JAVA_INCLUDE_PATH
    JAVA_INCLUDE_PATH2

Run the command with (you might need to change the path to Java include files and kfs source path):

    cmake -DJAVA_INCLUDE_PATH=/usr/lib/jvm/java-sun-6/include -DJAVA_INCLUDE_PATH2=/usr/lib/jvm/java-6-sun/inlclude/linux kfs-0.3/

then, in kfs source path:

    make

NOTICE: when building kfs, xfs development files might be necessary, so you should install xfslib-dev.

FTP storage
-----------

NFS storage
-----------

Corsair Carrier storage
-----------------------

Dependencies
============

+ libvirt
+ ruby-1.8.7
+ rails-2.3.2
+ xfslib-dev (xfs development files for compiling KFS)
+ kfs-0.3 (included in source)

