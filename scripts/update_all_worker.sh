#########################################################################
# Author: Wu Nuo
# Created Time: 2009年09月19日 星期六 11时42分56秒
# File Name: start_all_worker.sh
# Description: 
#########################################################################
#!/bin/bash
ssh node16 "cd /root/v2/worker && git pull && ./stop.sh && ./start.sh -d"
ssh node17 "cd /root/v2/worker && git pull && ./stop.sh && ./start.sh -d"
ssh node18 "cd /root/v2/worker && git pull && ./stop.sh && ./start.sh -d"
ssh node19 "cd /root/v2/worker && git pull && ./stop.sh && ./start.sh -d"
ssh node20 "cd /root/v2/worker && git pull && ./stop.sh && ./start.sh -d"

