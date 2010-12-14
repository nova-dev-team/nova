from usher.utils import notify, plugin
lg = notify.get_notify()
import copy
import math
import plugin
import datetime, time


class Algorithm(object):
    def __init__(self, args):
        self.greencomputing_threshold = args["greencomputing_threshold"]
        self.hot_threshold = args["hot"]
        self.warm_threshold = args["warm"]
        self.cold_threshold = args["cold"]
        self.limit_threshold = args["coldlimit"]
        self.max_mig_num = args["max_mig_num"] # maximum number allowed in the migration
        self.hotspots = []
        self.coldspots = []
        self.predicted_vms = []
        self.predicted_pms = []
        self.active_pms = []
        self.predicted_sys = None
        self.layout = {}
        self.noncoldspots = []
        self.max_skewness = 1.0e10
        self.iteration = 0

    #the predicted value and layout will reset to empty or none
    def reset(self):
        self.hotspots = []
        self.coldspots = []
        self.predicted_vms = []
        self.predicted_pms = []
        self.predicted_sys = None
        self.layout = {}

    def schedule(self, layout, predicted_vms, predicted_pms, predicted_sys):   
        #reset all the data at the very beginning
        self.reset()
        
        #get the predicted data from the predictor
        self.layout = layout
        self.predicted_vms = predicted_vms
        self.predicted_pms = predicted_pms
        self.predicted_sys = predicted_sys

        vmnum = len(self.predicted_vms)
        pmnum = len(self.predicted_pms)

        #calculate the system capacity(later used in hot threshold defined)
        tmp_sys_cap = plugin.SYS()
        for pm in self.predicted_pms:
            tmp_sys_cap.cpu += pm.cap_cpu
            tmp_sys_cap.ram += pm.cap_ram
            tmp_sys_cap.net_rx += pm.cap_net
            tmp_sys_cap.net_tx += pm.cap_net




        #try to evaluate the hot threshold before trying
        basic_threshold = []
        basic_threshold.append(self.hot_threshold)
        basic_threshold.append(self.predicted_sys.cpu / tmp_sys_cap.cpu)
        basic_threshold.append(self.predicted_sys.ram / tmp_sys_cap.ram)
        basic_threshold.append(self.predicted_sys.net_rx / tmp_sys_cap.net_rx)
        basic_threshold.append(self.predicted_sys.net_tx / tmp_sys_cap.net_tx)
        basic_threshold.sort(reverse=True)
        hot_threshold = basic_threshold[0]
        
        #generate hot spot list
        self.generate_hotspots(self.hot_threshold)

        #sort the pm according to the overload of the hot spot
        self.sort_hotspot(self.hot_threshold)
        
        #solve the hotspot
        #       for each pm in the hot spot list
        #            find the expected destination for specific vm
        #       generate the migration list (hot)
        pmdict = {}
        vmdict = {}
        pmdict, vmdict = self.generate_dicts()
        
        migration_list = {}
        
        while  True:
            bDoneNum = 0
            hotspotnumber = len(self.hotspots)
            
            for hot_pm in self.hotspots:
               
                bSucc, tmp_ml = self.solve_hotspot(hot_threshold, hot_pm.fqdn, pmdict, vmdict, migration_list)
                if bSucc != True:
                    #hot spot can not be handled then adjust the threshold and try again

                    hot_threshold *= 1.1

                    migration_list = {}
                    self.generate_hotspots(hot_threshold)
                    self.sort_hotspot(hot_threshold)
                    pmdict, vmdict = self.generate_dicts()
                    break 
                bDoneNum += 1
            
            if bDoneNum == hotspotnumber:
                break

        lg.notify(notify.DEBUG, "csw hot migration_list: %s" % migration_list)
        #generate the active pm list (the pm which has at least one vm running on)
        self.active_pms = self.generate_apmlist(pmdict)

        
        #calculate the system capacity according to the active pm list
        sys_cap = plugin.SYS()
        for pm in self.active_pms:
            sys_cap.cpu += pm.cap_cpu
            sys_cap.ram += pm.cap_ram
            sys_cap.net_rx += pm.cap_net
            sys_cap.net_tx += pm.cap_net
        
        #check whether we need green computing
        if self.check_green_compute(sys_cap) == False:
            return migration_list
     
        self.generate_coldspots(self.active_pms)
        
        #sort all the pm according to the memory of the pm used
        self.sort_coldspot()
        
#        now we will try to free the pm after the sorting until any dimension of the system utilized level rise to 60percent
#        6 generate cold spot list
#        7 for each pm in the cold spot list
#        8   collect all the vm of this pm(try to find the destination for the vm so we can finally free this pm in the list)
#        9   use the same way to find the destination for the vm
#          generate the migration list (cold)

        
#        num = 0
#        tmp_sys_cap = sys_cap
#        solve_num = 0
#        self.noncoldspots = [pm for (pmf, pm) in pmdict.items() if len(pm.vms) != 0 and pm not in self.coldspots]
                    
#        while num < len(self.coldspots):
#            
#            
#            cold_pm = self.coldspots[num]
#                 
#            #cold spot is not cold anymore, just continue
#            if cold_pm.cpu_rate >= self.cold_threshold or \
#                cold_pm.net_rx_rate >= self.cold_threshold or \
#                cold_pm.net_tx_rate >= self.cold_threshold or \
#                cold_pm.ram_rate >= self.cold_threshold:
#                num += 1
#                self.noncoldspots.append(cold_pm)
#                continue
#            
#
#            bSucc, dismiss_ml = self.solve_coldspot(cold_pm.fqdn, pmdict, vmdict, self.coldspots, num + 1)
#            if bSucc == True:
#                for vmf, pmf in dismiss_ml.items():
#                    migration_list[vmf] = pmf
#                tmp_sys_cap.cpu -= cold_pm.cap_cpu
#                tmp_sys_cap.ram -= cold_pm.cap_ram
#                tmp_sys_cap.net_tx -= cold_pm.cap_net
#                tmp_sys_cap.net_rx -= cold_pm.cap_net
#                solve_num += 1
#               
#                #check whether green computing should stop
#                if self.predicted_sys.cpu / tmp_sys_cap.cpu > self.greencomputing_threshold or \
#                    self.predicted_sys.ram / tmp_sys_cap.ram > self.greencomputing_threshold or \
#                    self.predicted_sys.net_rx / tmp_sys_cap.net_rx > self.greencomputing_threshold or \
#                    self.predicted_sys.net_tx / tmp_sys_cap.net_tx > self.greencomputing_threshold:
#                    break
#                #we can only solve specific cold spot each iteration
#                if solve_num > self.limit_threshold * pmnum:
#                    break
#                
#            else:
#                self.noncoldspots.append(cold_pm)
#            num += 1

        solve_num = 0
        tmp_sys_cap = sys_cap
        self.noncoldspots = [pm for (pmf, pm) in pmdict.items() if len(pm.vms) != 0 and pm not in self.coldspots]

        while len(self.coldspots) != 0:
            
            cold_pm = self.coldspots.pop(0)
                
            bSucc, dismiss_ml = self.solve_coldspot(cold_pm.fqdn, pmdict, vmdict)
            if bSucc == True:
                solve_num += 1
                
                for vmf, pmf in dismiss_ml.items():
                    migration_list[vmf] = pmf
                    
                tmp_sys_cap.cpu -= cold_pm.cap_cpu
                tmp_sys_cap.ram -= cold_pm.cap_ram
                tmp_sys_cap.net_tx -= cold_pm.cap_net
                tmp_sys_cap.net_rx -= cold_pm.cap_net
                
                #check whether green computing should stop
                if self.predicted_sys.cpu / tmp_sys_cap.cpu > self.greencomputing_threshold or \
                    self.predicted_sys.ram / tmp_sys_cap.ram > self.greencomputing_threshold or \
                    self.predicted_sys.net_rx / tmp_sys_cap.net_rx > self.greencomputing_threshold or \
                    self.predicted_sys.net_tx / tmp_sys_cap.net_tx > self.greencomputing_threshold:
                    break
                #we can only solve specific cold spot each iteration
                if solve_num > self.limit_threshold * pmnum:
                    break
            else:
                self.noncoldspots.append(cold_pm)
            
        return migration_list


    def generate_dicts(self):
        pmdict = {}
        vmdict = {}
        for vm in self.predicted_vms:
            vmdict[vm.fqdn] = vm
        for pm in self.predicted_pms:
            pmdict[pm.fqdn] = copy.deepcopy(pm)
        for vmf, pmf in self.layout.items():
            pmdict[pmf].vms.append(vmdict[vmf])
        return pmdict, vmdict


    #generate hot spot list (hot spot POLOCY)
    #(check all the dimension of each pm, 
    #whenever a dimension exceeds the hot_threshold,
    #then this pm is considered as a hot spot and append in the list)
    #    
    def generate_hotspots(self, hot_threshold):
        self.hotspots = [hot_pm for hot_pm in self.predicted_pms
                         if hot_pm.cpu_rate > hot_threshold or
                         hot_pm.net_rx_rate > hot_threshold or
                         hot_pm.net_tx_rate > hot_threshold or
                         hot_pm.ram_rate > hot_threshold]
##QUESTION FROM CSW,IS IT SUITABLE TO USE THRESHOLD FOR RAM,OR WE MAY SET A Specific ram limitation


    #function to calculate the overload value
    #the "ol" is the summation of the variance of the actual and the threshold in each dimension    
    def get_overload(self, pm, hot_threshold):
        ol = 0.0;
        #only the exceeding part will be added
        if pm.cpu_rate > hot_threshold:
            ol += (pm.cpu_rate - hot_threshold) ** 2#notice **2 is the squaring in python
        if pm.ram_rate > hot_threshold:
            ol += (pm.ram_rate - hot_threshold) ** 2
        if pm.net_rx_rate > hot_threshold:
            ol += (pm.net_rx_rate - hot_threshold) ** 2
        if pm.net_tx_rate > hot_threshold:
            ol += (pm.net_tx_rate - hot_threshold) ** 2
        return ol

    #sort the pm according to the overload of the hot spot
    def sort_hotspot(self, hot_threshold):
        sorted_hotspots = []
        for hot_pm in self.hotspots:
            overload = self.get_overload(hot_pm, hot_threshold)#calculate the overload value of each hot spot
            sorted_hotspots.append((-1 * overload, hot_pm))#append the overload attribute to each hot spot
        #sort(sorted_hotspots) #CSW something is not grammatical correct
        sorted_hotspots.sort(key=lambda x: x[0])
        self.hotspots = [hot_pm for (overload, hot_pm) in sorted_hotspots]


    def solve_hotspot(self, hot_threshold, hot_pmname, pmdict, vmdict, migration_list):
      
        skew_vm_dict = {}
        skew_vm_list = []
        skew_pm_dict = {}
        min_skew_vm = ""
        min_skew_pm = ""
        now_vm = ""
        tmp_overload = 0.0
        
        past_pmskewness = (pmdict[hot_pmname].cpu_rate, pmdict[hot_pmname].ram_rate, pmdict[hot_pmname].net_rx_rate, pmdict[hot_pmname].net_tx_rate)
        
        #each time only one vm with the smallest skewness value will be chose to migrate
        for vm in pmdict[hot_pmname].vms:

            new_pm = copy.deepcopy(pmdict[hot_pmname])

            new_pm.cpu = pmdict[hot_pmname].cpu - vm.cpu
            new_pm.ram = pmdict[hot_pmname].ram - vm.ram
            new_pm.net_tx = pmdict[hot_pmname].net_tx - vm.net_tx
            new_pm.net_rx = pmdict[hot_pmname].net_rx - vm.net_rx
            new_pm.cpu_rate = new_pm.cpu / pmdict[hot_pmname].cap_cpu
            new_pm.ram_rate = new_pm.ram / pmdict[hot_pmname].cap_ram
            new_pm.net_rx_rate = new_pm.net_rx / pmdict[hot_pmname].cap_net
            new_pm.net_tx_rate = new_pm.net_tx / pmdict[hot_pmname].cap_net
            
            new_pmskewness = (new_pm.cpu_rate, new_pm.ram_rate, new_pm.net_rx_rate, new_pm.net_tx_rate)
            tmp_overload = self.get_overload(new_pm, hot_threshold)
            skew_vm_dict[vm.fqdn] = (tmp_overload, self.skewness_eval(new_pmskewness, past_pmskewness))

        # sort key->overload, skewness
        skew_vm_list = [k for (k, v) in sorted(skew_vm_dict.items(), key=lambda x: x[1])]


        #        min_skew_vmval = -1.0
#        if len(skew_vm_dict) != 0:
#            for key,val in skew_vm_dict.items():
#                if val < min_skew_vmval:
#                    min_skew_vmval = val
#                    min_skew_vm = key
#        else:
#            return (False,None)
        flag = False
        for now_vm in skew_vm_list:
            for pmname, pm in pmdict.items():
                des_pm = copy.deepcopy(pm)
                
                des_pm.cpu += vmdict[now_vm].cpu
                des_pm.ram += vmdict[now_vm].ram
                des_pm.net_tx += vmdict[now_vm].net_tx
                des_pm.net_rx += vmdict[now_vm].net_rx
                des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
                des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
                des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
                des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
                
                if self.get_overload(des_pm, hot_threshold) > 0:
                    continue

                now_skewness = (des_pm.cpu_rate, des_pm.ram_rate, des_pm.net_rx_rate, des_pm.net_tx_rate)
                past_skewness = (pm.cpu_rate, pm.ram_rate, pm.net_rx_rate, pm.net_tx_rate)
                skew_pm_dict[des_pm.fqdn] = self.skewness_eval(now_skewness, past_skewness)

            if len(skew_pm_dict) == 0:
                continue

            min_skew_pmval = self.max_skewness
            for key, val in skew_pm_dict.items():
                if val < min_skew_pmval:
                    min_skew_pmval = val
                    min_skew_pm = key
                    flag = True
                    
            if flag == True:
                break

        if flag == True:
            mvm = vmdict[now_vm]
            pmdict[hot_pmname].vms.remove(mvm)
            pmdict[min_skew_pm].vms.append(mvm)
            
            pmdict[min_skew_pm].cpu += mvm.cpu
            pmdict[min_skew_pm].ram += mvm.ram
            pmdict[min_skew_pm].net_tx += mvm.net_tx
            pmdict[min_skew_pm].net_rx += mvm.net_rx
            pmdict[min_skew_pm].cpu_rate = pmdict[min_skew_pm].cpu / pmdict[min_skew_pm].cap_cpu
            pmdict[min_skew_pm].ram_rate = pmdict[min_skew_pm].ram / pmdict[min_skew_pm].cap_ram
            pmdict[min_skew_pm].net_rx_rate = pmdict[min_skew_pm].net_rx / pmdict[min_skew_pm].cap_net
            pmdict[min_skew_pm].net_tx_rate = pmdict[min_skew_pm].net_tx / pmdict[min_skew_pm].cap_net
            
            pmdict[hot_pmname].cpu -= mvm.cpu
            pmdict[hot_pmname].ram -= mvm.ram
            pmdict[hot_pmname].net_tx -= mvm.net_tx
            pmdict[hot_pmname].net_rx -= mvm.net_rx
            pmdict[hot_pmname].cpu_rate = pmdict[hot_pmname].cpu / pmdict[hot_pmname].cap_cpu
            pmdict[hot_pmname].ram_rate = pmdict[hot_pmname].ram / pmdict[hot_pmname].cap_ram
            pmdict[hot_pmname].net_rx_rate = pmdict[hot_pmname].net_rx / pmdict[hot_pmname].cap_net
            pmdict[hot_pmname].net_tx_rate = pmdict[hot_pmname].net_tx / pmdict[hot_pmname].cap_net
            
            migration_list[now_vm] = min_skew_pm

        return (flag, migration_list)

    #skewness is variance
    def skewnesspm_eval(self, nowval):
        average = sum(nowval) / len(nowval)
        nowskewness = 0.0

        for dim in nowval:
            nowskewness += (dim - average) ** 2

        return nowskewness/ len(nowval)

    #skewness is variance
    def skewness_eval(self, nowval, pastval):
        nowskewness = 0.0
        pastskewness = 0.0
        
        average = sum(nowval) / len(nowval)
        for dim in nowval:
            nowskewness += (dim - average) ** 2

        average = sum(pastval) / len(pastval)
        for dim in pastval:
            pastskewness += (dim - average) ** 2

        return (nowskewness - pastskewness) / len(nowval)

    def generate_apmlist(self, pmdict):
        apm = []
        for pmfqdn, pm in pmdict.items():
            if len(pm.vms) != 0:
                apm.append(pm)
        return apm

    def check_green_compute(self, sys_cap):
        if self.predicted_sys.cpu / sys_cap.cpu < self.greencomputing_threshold and \
                self.predicted_sys.ram / sys_cap.ram < self.greencomputing_threshold and \
                self.predicted_sys.net_tx / sys_cap.net_tx < self.greencomputing_threshold and \
                self.predicted_sys.net_rx / sys_cap.net_rx < self.greencomputing_threshold:
            return True
        return False

#   cold spot standard:
#   lower than the cold spot threshold (pm)(for each dimension)
    def generate_coldspots(self, apmlist):
        #if the system state is ready then try to find the cold spot(pm)
        self.coldspots = [cold_pm for cold_pm in apmlist
                          if cold_pm.cpu_rate < self.cold_threshold and
                          cold_pm.net_rx_rate < self.cold_threshold and
                          cold_pm.net_tx_rate < self.cold_threshold and
                          cold_pm.ram_rate < self.cold_threshold ]


    def sort_coldspot(self):
        sorted_coldspots = []
        for cold_pm in self.coldspots:
            sorted_coldspots.append((cold_pm.ram, cold_pm))
        sorted_coldspots.sort(key=lambda x:x[0])
        self.coldspots = [cold_pm for (rams, cold_pm) in sorted_coldspots]



    def solve_coldspot(self, cold_pmname, pmdict, vmdict):
        skew_pm_dict = {}
        tmp_migration_list = {}
        coldflag = False
        
        tmp_noncoldspots = copy.deepcopy(self.noncoldspots)
        tmp_coldspots = copy.deepcopy(self.coldspots)
        
        for now_vm in pmdict[cold_pmname].vms:
            for dpm in tmp_noncoldspots:
                des_pm = copy.deepcopy(dpm)
                
                des_pm.cpu = des_pm.cpu + now_vm.cpu
                des_pm.ram = des_pm.ram + now_vm.ram
                des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
                des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
                des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
                des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
                des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
                des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
                
                if self.get_overload(des_pm, self.warm_threshold) > 0:
                    continue
                else:
                    now_skewness = (des_pm.cpu_rate, des_pm.ram_rate, des_pm.net_rx_rate, des_pm.net_tx_rate)
                    past_skewness = (dpm.cpu_rate, dpm.ram_rate, dpm.net_rx_rate, dpm.net_tx_rate)
                    skew_pm_dict[des_pm.fqdn] = self.skewness_eval(now_skewness, past_skewness)

            if len(skew_pm_dict) != 0:
                min_skew_pmval = self.max_skewness
                for key, val in skew_pm_dict.items():
                    if val < min_skew_pmval:
                        min_skew_pmval = val
                        min_skew_pm = key
                        
                tmp_migration_list[now_vm.fqdn] = min_skew_pm
                
                for des_pm in tmp_noncoldspots:
                    if des_pm.fqdn == min_skew_pm:
                        des_pm.cpu = des_pm.cpu + now_vm.cpu
                        des_pm.ram = des_pm.ram + now_vm.ram
                        des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
                        des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
                        des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
                        des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
                        des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
                        des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
                        
            else:
                num = len(tmp_coldspots) - 1
                while num >= 0:
                    dpm = tmp_coldspots[num]
                    
                    des_pm = copy.deepcopy(dpm)
                    
                    des_pm.cpu = des_pm.cpu + now_vm.cpu
                    des_pm.ram = des_pm.ram + now_vm.ram
                    des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
                    des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
                    des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
                    des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
                    des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
                    des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
                    
                    if self.get_overload(des_pm, self.warm_threshold) > 0:
                        num -= 1
                        continue
                    
                    now_skewness = (des_pm.cpu_rate, des_pm.ram_rate, des_pm.net_rx_rate, des_pm.net_tx_rate)
                    past_skewness = (dpm.cpu_rate, dpm.ram_rate, dpm.net_rx_rate, dpm.net_tx_rate)
                    skew_pm_dict[des_pm.fqdn] = self.skewness_eval(now_skewness, past_skewness)
                    num -= 1

                if len(skew_pm_dict) != 0:
                    min_skew_pmval = self.max_skewness
                    for key, val in skew_pm_dict.items():
                        if val < min_skew_pmval:
                            min_skew_pmval = val
                            min_skew_pm = key
                            coldflag = True
                            
                    tmp_migration_list[now_vm.fqdn] = min_skew_pm     
                    
                    for des_pm in tmp_coldspots:
                        if des_pm.fqdn == min_skew_pm:
                            des_pm.cpu = des_pm.cpu + now_vm.cpu
                            des_pm.ram = des_pm.ram + now_vm.ram
                            des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
                            des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
                            des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
                            des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
                            des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
                            des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
                else:
                    return False, None

        for vmf, pmf in tmp_migration_list.items():
            mvm = vmdict[vmf]
            pmdict[cold_pmname].vms.remove(mvm)
            pmdict[pmf].vms.append(mvm)
            
            pmdict[pmf].cpu += mvm.cpu
            pmdict[pmf].ram += mvm.ram
            pmdict[pmf].net_tx += mvm.net_tx
            pmdict[pmf].net_rx += mvm.net_rx
            pmdict[pmf].cpu_rate = pmdict[pmf].cpu / pmdict[pmf].cap_cpu
            pmdict[pmf].ram_rate = pmdict[pmf].ram / pmdict[pmf].cap_ram
            pmdict[pmf].net_rx_rate = pmdict[pmf].net_rx / pmdict[pmf].cap_net
            pmdict[pmf].net_tx_rate = pmdict[pmf].net_tx / pmdict[pmf].cap_net
            
        self.coldspots = tmp_coldspots
        self.noncoldspots = tmp_noncoldspots
                


        if coldflag == True:
            num = 0
            while num < len(self.coldspots):
                cold_pm = self.coldspots[num]
                
                if cold_pm.cpu_rate >= self.cold_threshold or \
                    cold_pm.net_rx_rate >= self.cold_threshold or \
                    cold_pm.net_tx_rate >= self.cold_threshold or \
                    cold_pm.ram_rate >= self.cold_threshold:
                        self.noncoldspots.append(cold_pm)
                        self.coldspots.remove(cold_pm)
                else:
                    num += 1;

            self.coldspots.sort(lambda x,y:cmp(x.ram,y.ram))
            
        return True, tmp_migration_list
    
#    def solve_coldspot(self, cold_pmname, pmdict, vmdict, coldspots, num):
#        skew_pm_dict = {}
#        tmp_num = num
#        tmp_migration_list = {}
#        coldflag = False
#
#        tmp_noncoldspots = copy.deepcopy(self.noncoldspots)
#        tmp_coldspots = copy.deepcopy(self.coldspots)
#        
#        for now_vm in pmdict[cold_pmname].vms:
#            for dpm in tmp_noncoldspots:
#                des_pm = copy.deepcopy(dpm)
#                past_des_pm = copy.deepcopy(dpm)
#                des_pm.cpu = des_pm.cpu + now_vm.cpu
#                des_pm.ram = des_pm.ram + now_vm.ram
#                des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
#                des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
#                des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
#                des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
#                des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
#                des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
#                if self.get_overload(des_pm, self.warm_threshold) > 0:
#                    continue
#                else:
#                    now_skewness = (des_pm.cpu_rate, des_pm.ram_rate, des_pm.net_rx_rate, des_pm.net_tx_rate)
#                    past_skewness = (past_des_pm.cpu_rate, past_des_pm.ram_rate, past_des_pm.net_rx_rate, past_des_pm.net_tx_rate)
#                    skew_pm_dict[(des_pm.fqdn)] = self.skewness_eval(now_skewness, past_skewness)
#
#            min_skew_pmval = self.max_skewness
#            if len(skew_pm_dict) != 0:
#                for key, val in skew_pm_dict.items():
#                    if val < min_skew_pmval:
#                        min_skew_pmval = val
#                        min_skew_pm = key
#                tmp_migration_list[now_vm.fqdn] = min_skew_pm
#                for des_pm in tmp_noncoldspots:
#                    if des_pm.fqdn == min_skew_pm:
#                        des_pm.cpu = des_pm.cpu + now_vm.cpu
#                        des_pm.ram = des_pm.ram + now_vm.ram
#                        des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
#                        des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
#                        des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
#                        des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
#                        des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
#                        des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
#                        
#            else:
#
#                while num < len(coldspots):
#                    
#                    dpm = tmp_coldspots[num]
#                    des_pm = copy.deepcopy(dpm)
#                    past_des_pm = copy.deepcopy(dpm)
#                    des_pm.cpu = des_pm.cpu + now_vm.cpu
#                    des_pm.ram = des_pm.ram + now_vm.ram
#                    des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
#                    des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
#                    des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
#                    des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
#                    des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
#                    des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
#                    if self.get_overload(des_pm, self.warm_threshold) > 0:
#                        num += 1
#                        continue
#                    else:
#                        now_skewness = (des_pm.cpu_rate, des_pm.ram_rate, des_pm.net_rx_rate, des_pm.net_tx_rate)
#                        past_skewness = (past_des_pm.cpu_rate, past_des_pm.ram_rate, past_des_pm.net_rx_rate, past_des_pm.net_tx_rate)
#                        skew_pm_dict[(des_pm.fqdn)] = self.skewness_eval(now_skewness, past_skewness)
#                    num += 1
#
#                min_skew_pmval = self.max_skewness
#                if len(skew_pm_dict) != 0:
#                    for key, val in skew_pm_dict.items():
#                        if val < min_skew_pmval:
#                            min_skew_pmval = val
#                            min_skew_pm = key
#                            coldflag = True
#                    tmp_migration_list[now_vm.fqdn] = min_skew_pm     
#                    for des_pm in tmp_coldspots:
#                        if des_pm.fqdn == min_skew_pm:
#                            des_pm.cpu = des_pm.cpu + now_vm.cpu
#                            des_pm.ram = des_pm.ram + now_vm.ram
#                            des_pm.net_tx = des_pm.net_tx + now_vm.net_tx
#                            des_pm.net_rx = des_pm.net_rx + now_vm.net_rx
#                            des_pm.cpu_rate = des_pm.cpu / des_pm.cap_cpu
#                            des_pm.ram_rate = des_pm.ram / des_pm.cap_ram
#                            des_pm.net_rx_rate = des_pm.net_rx / des_pm.cap_net
#                            des_pm.net_tx_rate = des_pm.net_tx / des_pm.cap_net
#                else:
#                    return False, None
#
#        for vmf, pmf in tmp_migration_list.items():
#            mvm = vmdict[vmf]
#            pmdict[cold_pmname].vms.remove(mvm)
#            pmdict[pmf].vms.append(mvm)
#            pmdict[pmf].cpu += mvm.cpu
#            pmdict[pmf].ram += mvm.ram
#            pmdict[pmf].net_tx += mvm.net_tx
#            pmdict[pmf].net_rx += mvm.net_rx
#            pmdict[pmf].cpu_rate = pmdict[pmf].cpu / pmdict[pmf].cap_cpu
#            pmdict[pmf].ram_rate = pmdict[pmf].ram / pmdict[pmf].cap_ram
#            pmdict[pmf].net_rx_rate = pmdict[pmf].net_rx / pmdict[pmf].cap_net
#            pmdict[pmf].net_tx_rate = pmdict[pmf].net_tx / pmdict[pmf].cap_net
#            
#            
#            if coldflag == True and pmdict[pmf] in coldspots:
#                for tmp_coldpm in coldspots:
#                    if tmp_coldpm.fqdn == pmdict[pmf].fqdn:
#                        tmp_coldpm = copy.deepcopy(pmdict[pmf])
#                        break
#            else:
#                for tmp_coldpm in self.noncoldspots:
#                    if tmp_coldpm.fqdn == pmdict[pmf].fqdn:
#                        tmp_coldpm = copy.deepcopy(pmdict[pmf])
#                        break
#            
#
#            
#        if coldflag == True:
#            num = tmp_num
#            while num < len(coldspots):
#                cold_pm = coldspots[num]
#                if cold_pm.cpu_rate > self.cold_threshold or \
#                    cold_pm.net_rx_rate > self.cold_threshold or \
#                    cold_pm.net_tx_rate > self.cold_threshold or \
#                    cold_pm.ram_rate > self.cold_threshold:
#                        self.noncoldspots.append(cold_pm)
#                       
#                else:
#                    i = num - 1
#                    while i >= tmp_num:
#                        if coldspots[num].ram < coldspots[i].ram:
#                            coldspots[num], coldspots[i] = coldspots[i], coldspots[num]
#                            break
#                        else:
#                            i -= 1
#                num += 1
#
#        return True, tmp_migration_list



