
#
# $Id: README.txt 159 2008-09-20 05:44:36Z sriramsrao $
#
# Created on 2007/08/23
#
# Copyright 2008 Quantcast Corp.
# Copyright 2007 Kosmix Corp.
#
# This file is part of Kosmos File System (KFS).
#
# Licensed under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied. See the License for the specific language governing
# permissions and limitations under the License.
#
# Sriram Rao
# Quantcast Corp.
# (srao at quantcast dot com)

=================

Welcome to the Kosmos File System (KFS)!  The documentation is now on the
project Wiki:

http://kosmosfs.wiki.sourceforge.net/

KFS is being released under the Apache 2.0 license. A copy of the license
is included in the file LICENSE.txt.


DIRECTORY ORGANIZATION
======================
 - kfs (top-level directory)
    |
    |---> conf            (sample config files)
    |---> examples        (Example client code for accessing KFS)
    |
    |---> src
           |
           |----> cc
                  |
                  |---> access          (Java/Python glue code)
                  |---> meta            (meta server code)
                  |---> chunk           (chunk server code)
                  |---> libkfsClient    (client library code)
                  |---> libkfsIO        (IO library used by KFS)
                  |---> common          (common declarations)
                  |---> fuse            (FUSE module for Linux)
                  |---> tools           (KFS tools)
           |
           |----> java
                  |---> org/kosmix/kosmosfs/access: Java wrappers to call KFS-JNI code
           |                  
           |----> python
                  |---> tests           (Python test scripts)

