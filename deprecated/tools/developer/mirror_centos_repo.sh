#!/bin/sh
# This script will mirror centos packages into local repository.

. /etc/profile

rsync="/usr/bin/rsync -azHv --delete --delay-updates"
wget="wget -m -np"

# the local mirror location
local=/nova/mirror/centos/

###############################################

# mirror the official repository
mirror_official=rsync://mirrors.kernel.org/centos
verlist="5.5" # you could choose to mirror several versions like "5 5.4 5.4"
archlist="i386 x86_64 SRPMS"
baselist="os updates addons extras centosplus contrib fasttrack"

for ver in $verlist
do
  for arch in $archlist
  do
    for base in $baselist
    do
      remote=$mirror_official/$ver/$base/$arch/
      mkdir -p $local/$ver/$base/$arch
      $rsync $remote $local/$ver/$base/$arch/
    done
  done
done

$rsync $mirror_official/RPM-GPG-KEY-CentOS-5 $local
$rsync $mirror_official/RPM-GPG-KEY-beta $local
$rsync $mirror_official/TIME $local
$rsync $mirror_official/timestamp.txt $local

##############################################

# mirror for xen repository
mirror_xen=http://mirrors.unxmail.com/xen
verlist_xen="xen3.3.1"
local_xen=/tmp/mirror/xen
for ver in $verlist_xen
do
	remote=$mirror_xen/$ver/
	mkdir -p $local/$ver/
	$wget $remote -P $local/$ver/
done

