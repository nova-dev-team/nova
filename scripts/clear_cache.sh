#########################################################################
# Author: Wu Nuo
# Created Time: 2009��09��19�� ������ 11ʱ41��18��
# File Name: clear_cache.sh
# Description: 
#########################################################################
#!/bin/bash

ssh node16 rm -r /root/v2/worker/tmp/work_site/storage_cache
ssh node17 rm -r /root/v2/worker/tmp/work_site/storage_cache
ssh node18 rm -r /root/v2/worker/tmp/work_site/storage_cache
ssh node19 rm -r /root/v2/worker/tmp/work_site/storage_cache
ssh node20 rm -r /root/v2/worker/tmp/work_site/storage_cache

