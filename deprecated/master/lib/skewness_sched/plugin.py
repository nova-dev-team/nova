import os
from predict import predictor
import copy
import datetime, time
from config import config


class VM(object):
    def __init__(self, fqdn):
        self.fqdn = fqdn
        self.cpu = 0.0
        self.ram = 0.0
        self.net_tx = 0.0
        self.net_rx = 0.0
        self.pmfqdn = ""

class PM(object):
    def __init__(self, fqdn):
        self.fqdn = fqdn
        self.cpu = 0.0
        self.ram = 0.0
        self.net_tx = 0.0
        self.net_rx = 0.0

        self.cap_cpu = 0.0
        self.cap_ram = 0.0
        self.cap_net = 0.0

        self.cpu_rate = 0.0
        self.ram_rate = 0.0
        self.net_rx_rate = 0.0
        self.net_tx_rate = 0.0
        self.vms = []

class SYS(object):
    def __init__(self):
        self.cpu = 0.0
        self.ram = 0.0
        self.net_tx = 0.0
        self.net_rx = 0.0

#to calculate the hotspot and coldspot time for skewness heuristic
hotspottime = 0.0
coldspottime = 0.0

class Schedule(object):
    def __init__(self):

        self.layout = config.layout
        self.timestamp = 0.0
        self.iteration = 0
        self.pmdict = {}
        for pmname, caps in config.capacity.items():
            pm = PM(pmname)
            pm.cap_cpu = caps["hw_num_cpus"] * caps["hw_cpu_MHz"] * 1000000 # Hz per second
            pm.cap_net = caps["hw_num_nics"] * caps["hw_link_Mbps"] * 1000000 / 8 # bytes per second
            pm.cap_ram = caps["hw_ram_MB"]
            self.pmdict[pmname] = pm

        self.migration_list = {}
        self.predicted_vms = []
        self.predicted_pms = []
        self.predicted_sys = None

        #initialize the predictor
        self.vm_load_predictor = eval("predictor.%s" % config.vm_load_predictor)
        self.vm_load_predictor_param = config.vm_load_predictor_param
        self.vm_lps = {}
        self.pm_load_predictor = eval("predictor.%s" % config.pm_load_predictor)
        self.pm_load_predictor_param = config.pm_load_predictor_param
        self.pm_lps = {}
        self.sys_load_predictor = eval("predictor.%s" % config.sys_load_predictor)
        self.sys_load_predictor_param = config.sys_load_predictor_param
        self.sys_lps = {}
        self.algorithm = config.algorithm(config.algorithm_args)

        newfile = file("output/%s_sched.out" % config.heuristic_name, "w")
        newfile.close()
        self.predicted_vmload = {}
        self.output_layout(self.iteration)

        #to help evaluate the time of the algorithm
        self.scheduletime = 0.0
        self.migratenum = 0
        self.hotspotnumbefore = 0
        self.hotspotnumafter = 0
        self.totalhotspotnum = 0
        self.totalcoldspotnum = 0
        self.pmnum = 0
        self.vmnum = 0
        self.hotspotcount = 0
        self.coldspotcount = 0
        self.hot_threshold = 0.9
        self.cold_threshold = 0.25



    def entry_point(self, *args):

        print self.iteration
        self.num = args[0][0]
        self.timestamp = int(args[0][0])
        current_stats = args[0][1]

        time1 = time.time()

        self.preprocess(current_stats)

        time2 = time.time()

        self.predict()

        #to calculate the total number of the vms and pms #change
        self.vmnum = len(self.predicted_vms)
        self.pmnum = len(self.predicted_pms)

        time3 = time.time()

        #record the preprocess and predict time
        timeeval = file("output/%s_timeeval(vm%dpm%d).txt" % (config.heuristic_name, self.vmnum, self.pmnum) , "a")
        print >> timeeval, "iteration", self.num
        print >> timeeval, "preprocess_time:", time2 - time1
        print >> timeeval, "predict_time:", time3 - time2
        overweight = 0.0
        maxoverweight = -1
        minoverweight = 100000
        for pm in self.predicted_pms:
            tmp_ol = self.get_overload(pm, config.algorithm_args["hot"])
            overweight += tmp_ol
            if maxoverweight < tmp_ol:
                maxoverweight = tmp_ol
            if minoverweight > tmp_ol:
                minoverweight = tmp_ol

        print >> timeeval, "overweight_value:", overweight / len(self.predicted_pms)
        print >> timeeval, "max_overweight:", maxoverweight
        print >> timeeval, "min_overweight:", minoverweight
        timeeval.close()


        self.hotspotnumbefore = self.hotspot_count(config.algorithm_args["hot"])
        self.coldspotnumbefore = self.coldspot_count(config.algorithm_args["cold"])
        self.totalhotspotnum += self.hotspotnumbefore
        self.totalcoldspotnum += self.coldspotnumbefore

        self.schedule()

        self.migrate()



        self.hotspotnumafter = self.hotspot_count(config.algorithm_args["hot"])

        timeeval = file("output/%s_timeeval(vm%dpm%d).txt" % (config.heuristic_name , self.vmnum , self.pmnum) , "a")
        time4 = time.time()
        print >> timeeval, "schedule_total_time:", time4 - time3
        self.scheduletime += time4 - time3

        print >> timeeval, "migration_num:", len(self.migration_list)
        print >> timeeval, "hotspot_num_before_migrate:", self.hotspotnumbefore
        print >> timeeval, "coldspot_num_before_migrate:", self.coldspotnumbefore
        print >> timeeval, "hotspot_num_after_migrate:", self.hotspotnumafter

        self.migratenum += len(self.migration_list)

        print >> timeeval, "##hotspot_total_time:", hotspottime
        print >> timeeval, "##coldspot_total_time:", coldspottime
        print >> timeeval, "##total_hotspot_num:", self.totalhotspotnum
        print >> timeeval, "##total_coldspot_num:", self.totalcoldspotnum
        print >> timeeval, "\n"
        timeeval.close()

        self.iteration += 1
        self.output_layout(self.iteration)

    def hotspot_count(self, hot_threshold):
        tmp_hotspots = [hot_pm for hot_pm in self.predicted_pms\
                         if hot_pm.cpu_rate > hot_threshold or\
                         hot_pm.net_rx_rate > hot_threshold or\
                         hot_pm.net_tx_rate > hot_threshold or\
                         hot_pm.ram_rate > hot_threshold]

        return len(tmp_hotspots)

    def coldspot_count(self, cold_threshold):
        apm = []
        for pm in self.predicted_pms:
            if pm.cpu_rate != 0.0 or pm.ram_rate != 0.0 or pm.net_rx_rate != 0.0  or pm.net_tx_rate != 0.0:
                apm.append(pm)

        tmp_coldspots = [cold_pm for cold_pm in apm\
                         if cold_pm.cpu_rate < cold_threshold and\
                         cold_pm.net_rx_rate < cold_threshold and\
                         cold_pm.net_tx_rate < cold_threshold and\
                         cold_pm.ram_rate < cold_threshold]

        return len(tmp_coldspots)

    def preprocess(self, current_stats):
        self.vms = []
        # set VMS
        # in load.py
        # CPU - how many CPU Hz are used per second
        # NET - how many bytes are processed each epoch
        # RAM - used in Megabytes
        for vmname, load in current_stats.items():
            vm = VM(vmname)
            for key, val in load.items():
                if key == "cpu_ns_1":
                    for devnum, metrics in val.items():
                        vm.cpu += metrics
                elif key == "tx_bytes_1":
                    for devnum, metrics in val.items():
                        vm.net_tx += metrics / 60
                elif key == "rx_bytes_1":
                    for devnum, metrics in val.items():
                        vm.net_rx += metrics / 60
                elif key == "ram_MB":
                    vm.ram += val
            vm.pmfqdn = self.layout[vm.fqdn]
            self.vms.append(vm)

    def predict(self):

        self.predicted_vmload = {}

        self.predict_vms()
        self.predict_pms()
        self.predict_sys()


        vmpredictfile = file("output/vm_predicted_load.py", "a")
        print >> vmpredictfile, [self.num + config.vm_load_predictor_param["pspan"], self.predicted_vmload], ","
        vmpredictfile.close()


        pmpredictfile = file("output/pm_predicted_load.py", "a")
        tmp_predicted_pm = {}
        for pmload in self.predicted_pms:
            pmdict = {}
            pmdict["cpu"] = pmload.cpu
            pmdict["ram"] = pmload.ram
            pmdict["net_tx"] = pmload.net_tx
            pmdict["net_rx"] = pmload.net_rx
            tmp_predicted_pm[pmload.fqdn] = pmdict
        print >> pmpredictfile, [self.num + config.pm_load_predictor_param["pspan"], tmp_predicted_pm], ","
        pmpredictfile.close()


        syspredictfile = file("output/sys_predicted_load.py", "a")
        tmp_predicted_sys = {}
        tmp_predicted_sys["cpu"] = self.predicted_sys.cpu
        tmp_predicted_sys["ram"] = self.predicted_sys.ram
        tmp_predicted_sys["net_tx"] = self.predicted_sys.net_tx
        tmp_predicted_sys["net_rx"] = self.predicted_sys.net_rx
        print >> syspredictfile, [self.num + config.sys_load_predictor_param["pspan"], tmp_predicted_sys], ","
        syspredictfile.close()

    def predict_vms(self):
        self.predicted_vms = []
        tmp_vm_state = {
                "cpu": 0.0,
                "ram": 1,
                "net_tx": 0.0,
                "net_rx": 0.0,
        }
        for vm in self.vms:
            tmp_vm_state = {
                "cpu": 0.0,
                "ram": 1,
                "net_tx": 0.0,
                "net_rx": 0.0,
            }
            p_vm = VM(vm.fqdn)
            if self.vm_lps.get(vm.fqdn) is None:
                self.vm_lps[vm.fqdn] = {}

            if self.vm_lps[vm.fqdn].get("cpu") is None:
                self.vm_lps[vm.fqdn]["cpu"] = \
                        self.vm_load_predictor(**self.vm_load_predictor_param)
            span, p_vm.cpu = self.vm_lps[vm.fqdn]["cpu"].predict(vm.cpu)

            if self.vm_lps[vm.fqdn].get("ram") is None:
                self.vm_lps[vm.fqdn]["ram"] = \
                        self.vm_load_predictor(**self.vm_load_predictor_param)
            span, p_vm.ram = self.vm_lps[vm.fqdn]["ram"].predict(vm.ram)

            if self.vm_lps[vm.fqdn].get("net_tx") is None:
                self.vm_lps[vm.fqdn]["net_tx"] = \
                        self.vm_load_predictor(**self.vm_load_predictor_param)
            span, p_vm.net_tx = self.vm_lps[vm.fqdn]["net_tx"].predict(vm.net_tx)

            if self.vm_lps[vm.fqdn].get("net_rx") is None:
                self.vm_lps[vm.fqdn]["net_rx"] = \
                        self.vm_load_predictor(**self.vm_load_predictor_param)
            span, p_vm.net_rx = self.vm_lps[vm.fqdn]["net_rx"].predict(vm.net_rx)

            tmp_vm_state["cpu"] = p_vm.cpu
            tmp_vm_state["ram"] = p_vm.ram
            tmp_vm_state["net_tx"] = p_vm.net_tx
            tmp_vm_state["net_rx"] = p_vm.net_rx
            self.predicted_vmload[vm.fqdn] = tmp_vm_state
            self.predicted_vms.append(p_vm)


    def predict_pms(self):
        self.predicted_pms = []
        pmdict = {}

        for pmname, caps in config.capacity.items():
            pmdict[pmname] = PM(pmname)

        for vm in self.vms:
            pmf = self.layout[vm.fqdn]
            pmdict[pmf].cpu += vm.cpu
            pmdict[pmf].ram += vm.ram
            pmdict[pmf].net_tx += vm.net_tx
            pmdict[pmf].net_rx += vm.net_rx


        for pmf, pm in pmdict.items():
            p_pm = copy.deepcopy(self.pmdict[pmf])
            #the rate of each dimension is calculated

            p_pm.cpu_rate = pm.cpu / self.pmdict[pmf].cap_cpu
            p_pm.ram_rate = pm.ram / self.pmdict[pmf].cap_ram
            p_pm.net_rx_rate = pm.net_rx / self.pmdict[pmf].cap_net
            p_pm.net_tx_rate = pm.net_tx / self.pmdict[pmf].cap_net
            #print p_pm.cpu_rate,p_pm.ram_rate
            if p_pm.cpu_rate > self.hot_threshold or p_pm.ram_rate > self.hot_threshold or p_pm.net_rx_rate > self.hot_threshold or p_pm.net_tx_rate > self.hot_threshold:
                self.hotspotcount += 1
            if p_pm.cpu_rate < self.cold_threshold and p_pm.ram_rate < self.cold_threshold and p_pm.net_rx_rate < self.cold_threshold and p_pm.net_tx_rate < self.cold_threshold:
                if p_pm.cpu_rate != 0.0 or p_pm.ram_rate != 0.0 or p_pm.net_rx_rate != 0.0  or p_pm.net_tx_rate != 0.0:
                    self.coldspotcount += 1


            if self.pm_lps.get(pmf) is None:
                self.pm_lps[pmf] = {}

            if self.pm_lps[pmf].get("cpu") is None:
                self.pm_lps[pmf]["cpu"] = \
                        self.pm_load_predictor(**self.pm_load_predictor_param)
            span, p_pm.cpu = self.pm_lps[pmf]["cpu"].predict(pm.cpu)

            if self.pm_lps[pmf].get("ram") is None:
                self.pm_lps[pmf]["ram"] = \
                        self.pm_load_predictor(**self.pm_load_predictor_param)
            span, p_pm.ram = self.pm_lps[pmf]["ram"].predict(pm.ram)

            if self.pm_lps[pmf].get("net_tx") is None:
                self.pm_lps[pmf]["net_tx"] = \
                        self.pm_load_predictor(**self.pm_load_predictor_param)
            span, p_pm.net_tx = self.pm_lps[pmf]["net_tx"].predict(pm.net_tx)

            if self.pm_lps[pmf].get("net_rx") is None:
                self.pm_lps[pmf]["net_rx"] = \
                        self.pm_load_predictor(**self.pm_load_predictor_param)
            span, p_pm.net_rx = self.pm_lps[pmf]["net_rx"].predict(pm.net_rx)

            #the rate of each dimension is calculated
            p_pm.cpu_rate = p_pm.cpu / p_pm.cap_cpu
            p_pm.ram_rate = p_pm.ram / p_pm.cap_ram
            p_pm.net_rx_rate = p_pm.net_rx / p_pm.cap_net
            p_pm.net_tx_rate = p_pm.net_tx / p_pm.cap_net

            self.predicted_pms.append(p_pm)

        timeeval = file("output/%s_timeeval(vm%dpm%d).txt" % (config.heuristic_name , len(self.predicted_vms) , len(self.predicted_pms) ), "a")
        #timeeval = file("output/%s_timeeval(vm%dpm%d).txt" % (config.heuristic_name , 0, 0 ), "a")*change
        print >> timeeval,"hotspotcount_from_raw_data:",self.hotspotcount
        print >> timeeval,"coldspotcount_from_raw_data:",self.coldspotcount
        timeeval.close()

    def predict_sys(self):
        sys = SYS()
        self.predicted_sys = SYS()
        for vm in self.vms:
            sys.cpu += vm.cpu
            sys.ram += vm.ram
            sys.net_rx += vm.net_rx
            sys.net_tx += vm.net_tx

        if self.sys_lps is None:
            self.sys_lps = {}

        if self.sys_lps.get("cpu") is None:
            self.sys_lps["cpu"] = \
                    self.sys_load_predictor(**self.sys_load_predictor_param)
        span, self.predicted_sys.cpu = self.sys_lps["cpu"].predict(sys.cpu)

        if self.sys_lps.get("ram") is None:
            self.sys_lps["ram"] = \
                    self.sys_load_predictor(**self.sys_load_predictor_param)
        span, self.predicted_sys.ram = self.sys_lps["ram"].predict(sys.ram)

        if self.sys_lps.get("net_tx") is None:
            self.sys_lps["net_tx"] = \
                    self.sys_load_predictor(**self.sys_load_predictor_param)
        span, self.predicted_sys.net_tx = self.sys_lps["net_tx"].predict(sys.net_tx)

        if self.sys_lps.get("net_rx") is None:
            self.sys_lps["net_rx"] = \
                    self.sys_load_predictor(**self.sys_load_predictor_param)
        span, self.predicted_sys.net_rx = self.sys_lps["net_rx"].predict(sys.net_rx)


    def schedule(self):
        self.migration_list = \
            self.algorithm.schedule(self.layout, self.predicted_vms, self.predicted_pms,
                                    self.predicted_sys)

    def migrate(self):
        print self.migration_list

        f_migrate = file("output/%s_migrate.out" % config.heuristic_name, "a")
        print >> f_migrate, "\nafter iteration: %d" % self.iteration
        print >> f_migrate, self.migration_list
        f_migrate.close()

        for vmf, des_pmf in self.migration_list.items():
            self.pm_lps[self.layout[vmf]]["cpu"].reset()
            self.pm_lps[self.layout[vmf]]["ram"].reset()
            self.pm_lps[self.layout[vmf]]["net_tx"].reset()
            self.pm_lps[self.layout[vmf]]["net_rx"].reset()
            self.layout[vmf] = des_pmf
            self.pm_lps[des_pmf]["cpu"].reset()
            self.pm_lps[des_pmf]["ram"].reset()
            self.pm_lps[des_pmf]["net_tx"].reset()
            self.pm_lps[des_pmf]["net_rx"].reset()
            self.layout[vmf] = des_pmf



    def output_layout(self, iter):
        f_sched = file("output/%s_sched.out" % config.heuristic_name, "a")
        f_sched.writelines("##%s##"
                           % datetime.datetime.utcfromtimestamp(time.time()))
        # layout
        print >> f_sched, "\nafter iteration: %d" % iter
        for vmf, pmf in self.layout.items():
            print >> f_sched, "\t%s in %s" % (vmf, pmf)
        f_sched.close()

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
