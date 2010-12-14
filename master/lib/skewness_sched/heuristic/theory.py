import plugin

(T_item, S_item, L_item, H_item) = range(4)
(Unused, H_bin, L_bin, LT_bin, unfilled_LT_bin, S_bin, SS_bin,
T_bin, unfilled_T_bin) = range(4, 13)
checkdic = ["T_item", "S_item", "L_item", "H_item", "Unused", "H_bin", "L_bin", "LT_bin", "unfilled_LT_bin", "S_bin", "SS_bin",
"T_bin", "unfilled_T_bin"]

class Algorithm(object):
    def __init__(self, args):
        self.is_first = True
        self.bin_dict = {}#bintype + dic->{pmname,pm}
        self.vm_load_dict = {}#mark the statement of the last iteration
        self.pm_type_dict = {}#type
        self.sum = 0
        self.unwanted_pm_fqdn = ""
        #self.hot_threshold = args["hot"]
        #self.cold_threshold = args["cold"]
        #self.warm_threshold = args["warm"]
        #self.greencomputing_threshold = args["greencomputing_threshold"]
        #self.dispersedness = args["dispersedness"]
        self.hot_threshold = 0.9
        self.cold_threshold = 0.25
        self.warm_threshold = 1.0
        self.greencomputing_threshold = 0.4
        self.dispersedness = 20.0
        self.unhandled = False
        self.hotspots = []
        self.coldspots = []
        self.changed_vm = []
        self.changed_pm = []
        self.shutdown_bin = []
        
    def schedule(self, layout, predicted_vms, predicted_pms, predicted_sys):
        self.unwanted_pm_fqdn = ""
        self.unhandled = False
        self.reset()
        # init load
        self.layout = layout
        self.predicted_vms = predicted_vms
        self.predicted_pms = predicted_pms
        self.predicted_sys = predicted_sys
        
        for i in range(4, 13):
            self.bin_dict[i] = {}
        
        if self.is_first:
            # first time
            # re-insert all vms
            self.is_first = False
            self.setup_for_first()
            for vm_fqdn, vm in self.vmdict.items():
                self.insert(vm)
                #unhandle case
                if self.unhandled == True:
                    break
                self.check()
        else:
            # setup
            self.setup()
            if self.judge() == False:
                return {}
            
            # not first time
            self.shutdown_bin = []
            for vm in self.vmdict.values():
                if vm.max_load == vm.new_max_load:
                    continue
                vm.old_load = vm.max_load
                vm.max_load = vm.new_max_load
                vm.old_type = vm.type
                if vm.max_load <= 1.0 / 3:
                    vm.type = T_item
                elif vm.max_load <= 1.0 / 2:
                    vm.type = S_item
                elif vm.max_load <= 2.0 / 3:
                    vm.type = L_item
                else:
                    vm.type = H_item
                    
                self.changed_pm = []
                self.changed_vm = []
                self.change(vm)
                if self.unhandled == True:
                    self.recover(vm)
                    self.unhandled = False
                self.check()

        #####################
        # output sth
        #typefile = file("output/theory_type.txt", "a")
        
#        for pmname, pm in self.pmdict.items():
#            print >> typefile, pmname, checkdic[pm.type]
#            for vm in pm.vms:
#                print >> typefile, checkdic[vm.type], vm.max_load
#        self.sum += len(self.predicted_pms) - len(self.bin_dict[Unused])
#        typefile.close()
        #####################

        if self.unhandled == False:
            for vm in self.vmdict.values():
                if layout[vm.fqdn] != vm.pm.fqdn:
                    self.migration_list[vm.fqdn] = vm.pm.fqdn
            print "migration number: ", len(self.migration_list)
    
            #####################
            # record vm load and pm type
            for vm in self.vmdict.values():
                self.vm_load_dict[vm.fqdn] = vm.max_load
            for pm in self.pmdict.values():
                self.pm_type_dict[pm.fqdn] = pm.type
            #####################

        return self.migration_list

    ###################################
    # check the result
    ###################################
    def check(self):
        if len(self.bin_dict[T_bin]) > 0 or len(self.bin_dict[unfilled_T_bin]) > 0:
            if len(self.bin_dict[L_bin]) > 0 or len(self.bin_dict[unfilled_LT_bin]) > 0:
                print "Unexpected Result1!"
        if len(self.bin_dict[unfilled_T_bin]) > 1 or len(self.bin_dict[S_bin]) > 1:
            print "Unexpected Result2!"


    ###################################
    # the predicted value and layout will reset to empty or none
    ###################################
    def reset(self):
        # predicted load & layout
        self.predicted_vms = []
        self.predicted_pms = []
        self.predicted_sys = None
        self.layout = {}
        self.orig_layout = {}
        # return value
        self.migration_list = {}
        # two dicts
        self.pmdict = {}
        self.vmdict = {}

    def setup_for_first(self):
        ###############################
        # vm.pms
        # pm.vms
        self.generate_dicts_not_copy()
        for vm in self.vmdict.values():
            vm.pm = None
        ###############################

        ###############################
        # vm.new_max_load
        # pm.vms (update)
        self.cal_vm_load()
        for pm in self.predicted_pms:
            pm.vms = []
    
        ###############################
        # pm.type
        # And setup self.bin_dict (add all to Unused)
        for pm_fqdn, pm in self.pmdict.items():
            pm.type = Unused
            self.bin_dict[pm.type][pm_fqdn] = pm
        ###############################

        ###############################
        # vm.max_load
        # vm.type
        for vm_fqdn, vm in self.vmdict.items():
            vm.max_load = vm.new_max_load
            if vm.max_load <= 1.0 / 3:
                vm.type = T_item
            elif vm.max_load <= 1.0 / 2:
                vm.type = S_item
            elif vm.max_load <= 2.0 / 3:
                vm.type = L_item
            else:
                vm.type = H_item
        ###############################

    ###################################
    # set up attributes in self.predicted_pms
    ###################################
    def setup(self):
        # the following lines compute:
        # vm.max_load
        # vm.type
        ###############################
        # vm.pm
        # pm.vms
        self.generate_dicts_not_copy()
        ###############################
        self.cal_vm_load()            
        ###############################
        # pm.type
        # And setup self.bin_dict ()
        for pm in self.pmdict.values():
            pm.type = self.pm_type_dict[pm.fqdn]
            self.bin_dict[pm.type][pm.fqdn] = pm
        ###############################

        ###############################
        # vm.max_load
        # vm.type
        for vm in self.vmdict.values():
            vm.max_load = self.vm_load_dict[vm.fqdn]
            if vm.max_load <= 1.0 / 3:
                vm.type = T_item
            elif vm.max_load <= 1.0 / 2:
                vm.type = S_item
            elif vm.max_load <= 2.0 / 3:
                vm.type = L_item
            else:
                vm.type = H_item
        ###############################

    ###############################
    # vm.new_max_load
    def cal_vm_load(self):
        for pm in self.predicted_pms:
            # we would fill the following and use them
            pm.total = []
            # cpu
            pm.total.append(pm.cap_cpu * self.warm_threshold)
            # ram
            pm.total.append(pm.cap_ram * self.warm_threshold)
            # net_rx
            pm.total.append(pm.cap_net * self.warm_threshold)
            # net_tx
            pm.total.append(pm.cap_net * self.warm_threshold)
            # compute vm load
            for vm in pm.vms:
                # load
                vm.load = []
                # cpu
                vm.load.append(vm.cpu / pm.total[0])
                # ram
                vm.load.append(vm.ram / pm.total[1])
                # net_rx
                vm.load.append(vm.net_rx / pm.total[2])
                # net_tx
                vm.load.append(vm.net_tx / pm.total[3])
                # max dim
                vm.new_max_load = max(vm.load)
               
                for i in range(int(self.dispersedness)):
                    if vm.new_max_load <= (i + 1) / self.dispersedness:
                        vm.new_max_load = (i + 1) / self.dispersedness
                        break

    ###################################
    # generate dicts and not use pm predicted load
    ###################################
    def generate_dicts_not_copy(self):
        for vm in self.predicted_vms:
            self.vmdict[vm.fqdn] = vm
        for pm in self.predicted_pms:
            self.pmdict[pm.fqdn] = pm
            pm.cpu = 0
            pm.ram = 0
            pm.net_rx = 0
            pm.net_tx = 0
        for vmf, pmf in self.layout.items():
            self.pmdict[pmf].cpu += self.vmdict[vmf].cpu
            self.pmdict[pmf].ram += self.vmdict[vmf].ram
            self.pmdict[pmf].net_rx += self.vmdict[vmf].net_rx
            self.pmdict[pmf].net_tx += self.vmdict[vmf].net_tx
            self.pmdict[pmf].vms.append(self.vmdict[vmf])
            self.vmdict[vmf].pm = self.pmdict[pmf]

    ###################################
    # insert a vm 
    ###################################
    def insert(self, vm):
        if vm.type == H_item:
            self.new([vm])
        elif vm.type == L_item:
            new_bin = self.new([vm])
            if new_bin == None:
                return
            self.fill(new_bin)
        elif vm.type == S_item:
            self.insert_S_item(vm)
        else:
            self.fill_with([vm])

    ###################################
    # insert a S_item
    ###################################
    def insert_S_item(self, vm):
        if self.exist_z_bin(S_bin) == True:
            dest = self.get_z_bin(S_bin)
            self.move([vm], dest)
        else:
            self.new([vm])

    ###################################
    # use a new pm by insert the vm
    ###################################
    def new(self, vms):
        if self.exist_z_bin(Unused) == False:
            #unhandle cases
            #print "unhandled cases"
            self.unhandled = True
            return None

        if len(self.shutdown_bin) > 0:
            new_bin = self.shutdown_bin[0]
            self.shutdown_bin.remove(new_bin)
        else:
            new_bin = self.get_z_bin(Unused)

        src = vms[0].pm
        for i in range(len(vms)):
            vms[i].pm = new_bin
            if(src is not None):
                src.vms.remove(vms[i])
                self.changed_vm.append([vms[i], src, new_bin])
        if(src is not None):
            self.update_bin_type(src)

        new_bin.vms = []
        new_bin.vms.extend(vms)
        self.update_bin_type(new_bin)
        return new_bin

    ###################################
    # get remaining space
    ###################################
    def gap(self, pm):
        ret = 1
        for i in range(len(pm.vms)):
            ret -= pm.vms[i].max_load
        return ret

    ###################################
    # fill a bin with group
    ###################################
    def fill(self, pm):
        while self.gap(pm) >= 1.0 / 3 and \
            (self.exist_z_bin(T_bin) or  self.exist_z_bin(unfilled_T_bin)):
            if self.exist_z_bin(unfilled_T_bin):
                src = self.get_z_bin(unfilled_T_bin)
            else:
                src = self.get_z_bin(T_bin)
            dest = pm
            g = self.get_group(src)
            self.move(g, dest)


    ###################################
    # fill with a group
    ###################################
    def fill_with(self, vms):
        if self.exist_z_bin(L_bin):
            dest = self.get_z_bin(L_bin)
            self.move(vms, dest)
        elif self.exist_z_bin(unfilled_LT_bin):
            dest = self.get_z_bin(unfilled_LT_bin)
            self.move(vms, dest)
        elif self.exist_z_bin(unfilled_T_bin):
            dest = self.get_z_bin(unfilled_T_bin)
            self.move(vms, dest)
        else:
            self.new(vms)

    ###################################
    # get a group from a bin
    ###################################
    def get_group(self, pm):
        group = []
        sum_load = 0.0
        for i in range(len(pm.vms)):
            vm = pm.vms[i]
            if vm.type == T_item and sum_load + vm.max_load <= 1.0 / 3:
                group.append(vm)
                sum_load += vm.max_load#code review
        return group

    ###################################
    # move one or more vms to pm
    ###################################
    def move(self, vmlist, dest):
        src = vmlist[0].pm
        for i in range(len(vmlist)):
            vm = vmlist[i]
            vm.pm = dest
            dest.vms.append(vm)

            if src is not None:
                src.vms.remove(vm)
                self.changed_vm.append([vmlist[i], src, dest])
        if src is not None:
#            del self.bin_dict[src.type][src.fqdn]
#            self.get_bin_type(src)
#            self.bin_dict[src.type][src.fqdn] = src
            self.update_bin_type(src)
#        del self.bin_dict[dest.type][dest.fqdn]
#        self.get_bin_type(dest)
#        self.bin_dict[dest.type][dest.fqdn] = dest
        self.update_bin_type(dest);

    ###################################
    # set the type of a bin
    ###################################
    def get_bin_type(self, pm):
        if pm.fqdn == self.unwanted_pm_fqdn:
            pm.type = Unused
            return
        vm_type_dict = {}
        if len(pm.vms) == 0:
            pm.type = Unused
            return
        for i in range(len(pm.vms)):
            vm = pm.vms[i]
            if vm.type in vm_type_dict:
                vm_type_dict[vm.type].append(vm)
            else:
                vm_type_dict[vm.type] = [vm]

        if H_item in vm_type_dict:
            if len(pm.vms) == 1:
                pm.type = H_bin
            else:
                print "Wrong Type 1!"
        elif L_item in vm_type_dict:
            if S_item in vm_type_dict or len(vm_type_dict[L_item]) > 1:
                print "Wrong Type! 2"
            elif T_item in vm_type_dict:
                if self.gap(pm) < 1.0 / 3:
                    pm.type = LT_bin
                else:
                    pm.type = unfilled_LT_bin
            else:
                pm.type = L_bin
        elif S_item in vm_type_dict:
            if T_item in vm_type_dict:
                print "Wrong Type! 3"
            elif len(pm.vms) == 2:#code review
                pm.type = SS_bin
            elif len(pm.vms) == 1:
                pm.type = S_bin
            else:
                print "wrong type 4"
        else:
            if self.gap(pm) < 1.0 / 3:
                pm.type = T_bin
            else:
                pm.type = unfilled_T_bin

    ###################################
    # vm change 
    ###################################
    def change(self, vm):
        old_pm = vm.pm
        self.unwanted_pm_fqdn = vm.pm.fqdn

        if vm.old_type == H_item:
            if vm.type == L_item:
                self.fill(old_pm)
            elif vm.type == S_item:
                if self.exist_z_bin(S_bin):
                    dest = self.get_z_bin(S_bin)
                    self.move([vm], dest)
            elif vm.type == T_item:
                if self.exist_z_bin(L_bin):
                    dest = self.get_z_bin(L_bin)
                    self.move([vm], dest)
                elif self.exist_z_bin(unfilled_LT_bin):
                    dest = self.get_z_bin(unfilled_LT_bin)
                    self.move([vm], dest)
                elif self.exist_z_bin(unfilled_T_bin):
                    dest = self.get_z_bin(unfilled_T_bin)
                    self.move([vm], dest)
        elif vm.old_type == L_item:
            if vm.type == H_item:
                self.release(old_pm)
                if self.unhandled == True:#unhandle case
                    return 
            elif vm.type == L_item:
                self.adjust(old_pm)
                if self.unhandled == True:#unhandle case
                    return
            elif vm.type == S_item:
                self.release(old_pm)
                if self.unhandled == True:#unhandle case
                    return
                if self.exist_z_bin(S_bin):
                    self.move([vm], self.get_z_bin(S_bin))
            elif vm.type == T_item:
                if self.exist_z_bin(T_bin) or self.exist_z_bin(unfilled_T_bin):
                    while self.exist_z_bin(unfilled_T_bin):
                        group = self.get_group(old_pm)
                        if(len(group) == 0):
                            break
                        self.move(group, self.get_z_bin(unfilled_T_bin))
                else:
                    while self.exist_z_bin(L_bin) or self.exist_z_bin(unfilled_LT_bin):
                        group = self.get_group(old_pm)
                        if(len(group) == 0):
                            break;
                        if self.exist_z_bin(L_bin):
                            dest = self.get_z_bin(L_bin)
                        else:
                            dest = self.get_z_bin(unfilled_LT_bin)
                        self.move(group, dest)
        elif vm.old_type == S_item:
            if vm.type == H_item:
                if old_pm.type == SS_bin:
                    self.insert_S_item(self.get_another_S_item(vm))
                    if self.unhandled == True:#unhandle case
                        return
            elif vm.type == L_item:
                if old_pm.type == SS_bin:
                    self.insert_S_item(self.get_another_S_item(vm))
                    if self.unhandled == True:#unhandle case
                        return
                self.fill(old_pm)
            elif vm.type == T_item:
                if old_pm.type == SS_bin and self.exist_z_bin(S_bin):
                    item = self.get_another_S_item(vm)
                    bin = self.get_z_bin(S_bin)
                    self.move([item], bin)
                if self.exist_z_bin(L_bin):
                    self.move([vm], self.get_z_bin(L_bin))
                elif self.exist_z_bin(unfilled_LT_bin):
                    self.move([vm], self.get_z_bin(unfilled_LT_bin))
                elif self.exist_z_bin(unfilled_T_bin):
                    self.move([vm], self.get_z_bin(unfilled_T_bin))
                elif old_pm.type == SS_bin:
                    if self.new([vm]) == None:
                        return
        elif vm.old_type == T_item:
            if vm.type == H_item:
                if old_pm.type == LT_bin or old_pm.type == unfilled_LT_bin:
                    new_bin = self.new([self.get_L_item(old_pm, vm)])
                    if new_bin == None:
                        return
                    self.fill(new_bin)
                self.release(old_pm)
                if self.unhandled == True:
                    return 
            elif vm.type == L_item:
                if old_pm.type == LT_bin or old_pm.type == unfilled_LT_bin:
                    new_bin = self.new([self.get_L_item(old_pm, vm)])
                    if new_bin == None:
                        return
                    self.fill(new_bin)
                self.adjust(old_pm)
                if self.unhandled == True:
                    return
            elif vm.type == S_item:
                if old_pm.type == LT_bin or old_pm.type == unfilled_LT_bin:
                    orig_bin = old_pm
                    self.insert_S_item(vm)
                    if self.unhandled == True:
                        return 
                    self.fill(orig_bin)
                elif self.exist_z_bin(S_bin):
                    while self.exist_z_bin(unfilled_T_bin):
                        group = self.get_group(old_pm)
                        if len(group) == 0:
                            break;
                        self.move(group, self.get_z_bin(unfilled_T_bin))
                    self.move([vm], self.get_z_bin(S_bin))
                else:
                    self.release(old_pm)
                    if self.unhandled == True:
                        return
            elif vm.type == T_item:
                if old_pm.type == LT_bin or old_pm.type == unfilled_LT_bin:
                    self.adjust(old_pm)
                    if self.unhandled == True:
                        return
                elif self.gap(old_pm) < 0:
                    self.fill_with([vm])
                    if self.unhandled == True:
                        return
                else:
                    while self.gap(old_pm) >= 1.0 / 3 and self.exist_z_bin(unfilled_T_bin):
                        bin = self.get_z_bin(unfilled_T_bin)
                        self.move(self.get_group(bin), old_pm)


        self.unwanted_pm_fqdn = ""
        self.update_bin_type(old_pm)

    ###################################
    # return another S_item in SS_bin
    ###################################
    def get_another_S_item(self, vm):
        if vm.pm.vms[0] == vm:
            return vm.pm.vms[1]
        else:
            return vm.pm.vms[0]

    ###################################
    # return a L_item in a bin
    ###################################
    def get_L_item(self, pm, changed_vm):
        for i in range(len(pm.vms)):
            vm = pm.vms[i]
            if vm.type == L_item and vm.fqdn != changed_vm.fqdn:
                return vm

    ###################################
    # update the type of a bin
    ###################################
    def update_bin_type(self, pm):
        try:
            del self.bin_dict[pm.type][pm.fqdn]
        except:
            print "Exception!"
        
        type = pm.type
        self.get_bin_type(pm)
#        if pm.type not in self.bin_dict:
#            self.bin_dict[pm.type] = {}
        self.bin_dict[pm.type][pm.fqdn] = pm
        self.changed_pm.append([pm, type, pm.type])
        if pm.type == Unused and pm.fqdn != self.unwanted_pm_fqdn:
            self.shutdown_bin.append(pm)

    ###################################
    # remove all group in a pm 
    ###################################
    def release(self, pm):
        group = self.get_group(pm)
        while len(group) > 0:
            self.fill_with(group)
            if self.unhandled == True:
                return 
            group = self.get_group(pm)
        return

    ###################################
    # adjust a L_bin or a LT_bin 
    ###################################
    def adjust(self, pm):
        while self.gap(pm) < 0:
            group = self.get_group(pm)
            self.fill_with(group)
            if self.unhandled == True:
                return
        if self.gap(pm) >= 1.0 / 3:
            self.fill(pm)

    #########################################
    # query: if there exists some kind of bin 
    #########################################
    def exist_z_bin(self, type):
        if len(self.bin_dict[type]) > 1:
            return True
        elif len(self.bin_dict[type]) == 1:
            if self.bin_dict[type].values()[0].fqdn == self.unwanted_pm_fqdn:
                return False
            else:
                return True
        else:
            return False

    #########################################
    # return a bin of some kind 
    #########################################
    def get_z_bin(self, type):
        if self.bin_dict[type].values()[0].fqdn == self.unwanted_pm_fqdn:
            return self.bin_dict[type].values()[1]
        else:
            return self.bin_dict[type].values()[0]

    def judge(self):
        self.generate_hotspots(self.hot_threshold)
        if len(self.hotspots) > 0:
            return True
        
        #calculate the system capacity according to the active pm list
        sys_cap = plugin.SYS()
        apmlist = self.generate_apmlist()
        for pm in apmlist:
            sys_cap.cpu += pm.cap_cpu
            sys_cap.ram += pm.cap_ram
            sys_cap.net_rx += pm.cap_net
            sys_cap.net_tx += pm.cap_net
        
        self.generate_coldspots(apmlist)
        return self.check_green_compute(sys_cap) and len(self.coldspots) > 0 

    def generate_apmlist(self):
        apm = []
        for pm in self.pmdict.values():
            if len(pm.vms) != 0:
               apm.append(pm)
        return apm

    def generate_hotspots(self, hot_threshold):
        self.hotspots = [hot_pm for hot_pm in self.predicted_pms
                         if hot_pm.cpu_rate > hot_threshold or
                         hot_pm.net_rx_rate > hot_threshold or
                         hot_pm.net_tx_rate > hot_threshold or
                         hot_pm.ram_rate > hot_threshold]
        
    def generate_coldspots(self, apmlist):
        #if the system state is ready then try to find the cold spot(pm)
        self.coldspots = [cold_pm for cold_pm in apmlist
                          if cold_pm.cpu_rate < self.cold_threshold and
                          cold_pm.net_rx_rate < self.cold_threshold and
                          cold_pm.net_tx_rate < self.cold_threshold and
                          cold_pm.ram_rate < self.cold_threshold]
        
    def check_green_compute(self, sys_cap):
        if self.predicted_sys.cpu / sys_cap.cpu < self.greencomputing_threshold and \
                self.predicted_sys.ram / sys_cap.ram < self.greencomputing_threshold and \
                self.predicted_sys.net_tx / sys_cap.net_tx < self.greencomputing_threshold and \
                self.predicted_sys.net_rx / sys_cap.net_rx < self.greencomputing_threshold:
            
            return True
        return False

    def recover(self, cur_vm):
        cur_vm.type = cur_vm.old_type
        cur_vm.max_load = cur_vm.old_load
        Len = len(self.changed_vm)
        for i in range(Len):
            j = Len - i - 1
            vm = self.changed_vm[j][0]
            orig_pm = self.changed_vm[j][1]
            pm = self.changed_vm[j][2]
            vm.pm = orig_pm
            pm.vms.remove(vm)
            orig_pm.vms.append(vm)
        Len = len(self.changed_pm)
        for i in range(Len):
             j = Len - i - 1
             pm = self.changed_pm[j][0]
             orig_type = self.changed_pm[j][1]
             type = self.changed_pm[j][2]
             del self.bin_dict[type][pm.fqdn]
             self.bin_dict[orig_type][pm.fqdn] = pm

