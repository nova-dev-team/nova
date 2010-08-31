#!/bin/sh

. /etc/profile
rsync="/usr/bin/rsync -azHv --delete --delay-updates"

mirror=rsync://mirrors.kernel.org/centos
#verlist="5 5.1 5.2" ��Ҫʲô�汾��Դ�������
verlist="5.4"
archlist="i386 x86_64 SRPMS"
baselist="os updates addons extras centosplus contrib fasttrack"
#rsyncͬ�������ص�λ�ã����һ���汾��Ҫ25g�Ŀռ����5��5.2���һ����Ҫ50g�Ŀռ䣬5.3Ŀǰ���Ƚ�С
local=/tmp/mirror/

for ver in $verlist
do
        for arch in $archlist
        do
                for base in $baselist
                do
                        remote=$mirror/$ver/$base/$arch/
                        mkdir -p $local/$ver/$base/$arch
                        $rsync $remote $local/$ver/$base/$arch/


                done
        done
done

$rsync $mirror/RPM-GPG-KEY-CentOS-5 $local
$rsync $mirror/RPM-GPG-KEY-beta $local
$rsync $mirror/TIME $local
$rsync $mirror/timestamp.txt $local


#This xen is x86_64
mirror_xen=http://mirrors.unxmail.com/xen
verlist_xen="xen3.3.1"
local_xen=/tmp/mirror/xen
wget="wget -m -np"
for ver in $verlist_xen
do
	remote=$mirror_xen/$ver/
	mkdir -p $local/$ver/
	$wget $remote -P $local_xen
done
