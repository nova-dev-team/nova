#!/usr/bin/python

import plugin
import os
from config import config

def run():
    # init just once
    #os.mkdir("output")
    scheduler = plugin.Schedule()

    predictfile = file("output/predicted_load.py","w")
    print >> predictfile, "predicted_history = ["
    predictfile.close()

    # run total_iter times

    loadfile = file("config/load.py","r")
#    for arg in load.history:
#        scheduler.entry_point(arg)

    tmp = loadfile.readline()
    while True:
        tmp = loadfile.readline()
        if len(tmp) == 0:
            break
        beg = tmp.find("[")
        end = tmp.rfind("]")
        if beg == -1 or end == -1:
            break
        tmp = tmp[beg:end+1]

        arg = eval(tmp)

        if len(arg) == 0:
            break

        scheduler.entry_point(arg)

    predictfile = file("output/predicted_load.py","a")

    print >> predictfile, "]"
    predictfile.close()

#    timeeval = file("output/%s_timeeval.txt" % config.heuristic_name, "a")
    timeeval = file("output/%s_timeeval(vm%dpm%d).txt" % (config.heuristic_name,scheduler.vmnum,scheduler.pmnum) , "a")
    print >> timeeval, "\n###total_schedule_time:",scheduler.scheduletime
    print >> timeeval, "\n###total_migration_num:",scheduler.migratenum
    timeeval.close()

if __name__ == "__main__":
    run()
