#!/bin/sh
insert=0
delete=0
count=0

git log  --since="2013-08-04" --shortstat --pretty=format:""|sed /^$/d > .tmp.count
while read line ;do
    current=`echo $line|awk -F ',' '{printf $2}'|awk '{printf $1}'`
    insert=`expr $insert + $current`
    current=`echo $line|awk -F ',' '{printf $3}'|awk '{printf $1}'`
    if [ "$current" = "" ] ;then
        current=0
    fi
    delete=`expr $delete + $current`
done < .tmp.count
git log  --since="2013-08-04" --shortstat --pretty=format:"%ci"|sed /^$/d > .tmp.count
while read line ;do
    if echo $line|grep 2013-08 >/dev/null ;then
        count=`expr $count + 1`
    fi
done < .tmp.count
echo $count submissions, $insert insertions, $delete deletions
