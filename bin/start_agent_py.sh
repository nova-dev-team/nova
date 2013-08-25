#!/bin/bash

mkdir /Nova
cd /media/cdrom
cp -R * /Nova
cd /Nova/run/bin
/usr/bin/python /Nova/run/bin/setip.py >> /root/1.txt
/usr/bin/python /Nova/run/bin/start.py agent >> /root/2.txt
