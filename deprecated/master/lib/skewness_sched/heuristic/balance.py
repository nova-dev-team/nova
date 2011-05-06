COOL, WARM, HOT = range(3)

class Scheduler(object):

    def __init__(self, sim, args):
        self.sim = sim
        self.hot_threshold = args["hot"]
        self.warm_threshold = args["warm"]

    def schedule(self):
        # return value
        self.newschedule = {}
        # hot pms
        self.hot_pms = []
        # setup pm and vm load
        self.setup()
        # handle hot pms
        for (overload, pm) in self.hot_pms:
            move = self.remove_hotspot(pm)
            if move is not None:
                self.newschedule[move[0]] = move[1]
                move[1].freeram -= move[0].ram
                # update the pm info, vm would not be consider anyway
                for i in range(len(move[1].load)):
                    new_load = move[0].load[i] * move[0].pm.total[i]
                    new_load /= move[1].total[i]
                    move[1].load[i] += new_load
                # check validation here
                if not move[0].migrate(move[1]):
                    raise Exception("Invalid Migration...!!!")
                (type, balance) = self.compute_balance(move[1].load)
                if type == HOT:
                    raise Exception("generating new hotspot...!!!")
        return self.newschedule

    def setup(self):
        # init newschedule
        for vm in self.sim.vms:
            if not vm.fqdn.startswith("Domain-0"):
                self.newschedule[vm] = vm.pm
        # dom0 as exception when scheduling
        for pm in self.sim.pms:
            pm.vms_no_dom0 = []
            for vm in pm.vms:
                if not vm.fqdn.startswith("Domain-0"):
                    pm.vms_no_dom0.append(vm)
        # compute pm loads and find and sort hot pms
        # this algorithm would rely on only:
        # pm: vms_no_dom0, overload, freeram, load and total
        # vm: pm.total, load and ram
        for pm in self.sim.pms:
            pm.set_allocs()
            pm.load = []
            pm.overload = 0.0
            pm.freeram = 0
            pm.total = []
            for r in pm.resources:
                if r.key != "ram":
                    pm.load.append(r.consumed / r.total_capacity)
                    pm.total.append(r.total_capacity)
                else:
                    pm.freeram = r.free
            for l in pm.load:
                if l > self.hot_threshold:
                    pm.overload += (l - self.hot_threshold) ** 2
            if pm.overload > 0:
                self.hot_pms.append((pm.overload, pm))
            # compute vm load
            for vm in pm.vms_no_dom0:
                # ram
                vm.ram = vm.allocs["ram"][0][1]
                # load
                vm.load = []
                # cpu
                l_cpu = 0.0
                if "cpu" in vm.allocs:
                    for (devnum, c) in vm.allocs["cpu"]:
                        l_cpu += c
                vm.load.append(l_cpu / pm.total[0])
                # net_t
                l_tx = 0.0
                if "tx" in vm.allocs:
                    for (devnum, t) in vm.allocs["tx"]:
                        l_tx += t
                vm.load.append(l_tx / pm.total[1])
                # net_r
                l_rx = 0.0
                if "rx" in vm.allocs:
                    for (devnum, r) in vm.allocs["rx"]:
                        l_rx += r
                vm.load.append(l_rx / pm.total[2])
        self.hot_pms.sort()
        self.hot_pms.reverse()

    def remove_hotspot(self, pm):
        candidate_vms = {COOL: [], WARM: [], HOT: []}
        for vm in pm.vms_no_dom0:
            load_after_out = []
            for i in range(len(pm.load)):
                load_after_out.append(pm.load[i] - vm.load[i])
            (type, balance) = self.compute_balance(load_after_out)
            candidate_vms[type].append((balance / vm.ram, vm))
        for type in candidate_vms:
            candidate_vms[type].sort()
            move = self.select_dest(type, candidate_vms)
            if move is not None:
                return move

    def select_dest(self, vm_type, candidate_vms):
        for (cbal, cvm) in candidate_vms[vm_type]:
            valid_dests = []
            for dest in self.sim.pms:
                if cvm.ram > dest.freeram:
                    continue
                if cvm.pm.fqdn == dest.fqdn:
                    continue
                load_after_in = []
                for i in range(len(dest.load)):
                    l = cvm.load[i] * cvm.pm.total[i]
                    l /= dest.total[i]
                    l = dest.load[i] + l
                    load_after_in.append(l)
                (type, balance) = self.compute_balance(load_after_in)
                if type == HOT and vm_type == HOT:
                    continue
                    # more hotsopts but not very hot both
                    # if cbal + balance < cvm.pm.overload + dest.overload:
                    #     valid_dests.append((type, balance, dest))
                elif type <= vm_type:
                    valid_dests.append((type, balance, dest))
            if len(valid_dests) != 0:
                valid_dests.sort()
                return (cvm, valid_dests[0][2])

    def compute_balance(self, load):
        is_hot = False
        balance = 0.0
        for l in load:
            if l > self.hot_threshold:
                is_hot = True
                balance += (l - self.hot_threshold) ** 2
        if is_hot:
            return (HOT, balance)
        is_cool = True
        mean = 0.0
        for l in load:
            mean += l
            if l > self.warm_threshold:
                is_cool = False
                break
        mean /= len(load)
        balance = 0.0
        for l in load:
            balance += (l - mean) ** 2
        if is_cool:
            return (COOL, balance)
        else:
            return (WARM, balance)
