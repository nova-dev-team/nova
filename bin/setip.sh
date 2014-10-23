#! /bin/sh

declare -a array
count=1
while read LINE
do
	array[$count]=$LINE
	echo ${array[count]}
	count=${count}+1
done < /Nova/params/ipconfig.txt
ifconfig eth0 ${array[1]} netmask ${array[2]}
route add default gw ${array[3]}
echo ${array[4]} > /etc/hostname
