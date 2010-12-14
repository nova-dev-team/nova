SLEEP, COOL, WARM, NOT_HOT, HOT = range(5)

class Algorithm(object):
    ###################################
    # init
    ###################################
    def __init__(self, args):
        self.hot_threshold = args["hot"]
        self.warm_threshold = args["warm"]

    ###################################
    # schedule to generate mig list 
    ###################################
    def schedule(self, layout, predicted_vms, predicted_pms, predicted_sys):
        # reset
        self.reset()
        # init load
        self.layout = layout
        self.predicted_vms = predicted_vms
        self.predicted_pms = predicted_pms
        # are we allocing sleeping PM?
        self.alloc_sleep_pm = False
        # setup pm and vm load
        self.setup()
        # handle hot pms
        for pm in self.hotspots:
            move = self.remove_hotspot(pm)
            if move is not None:
                (before_type, before_penalty) = \
                    self.compute_penalty(move[1].load)
                if before_type == SLEEP:
                    # we are allocing a sleeping pm
                    self.alloc_sleep_pm = True
                # move it!
                self.migration_list[move[0].fqdn] = move[1].fqdn
                # not update the hotspot to prevent sth move back in
                # update the dest info, vm would not be consider anyway
                # because only vm on remaining hotspot or light pms 
                # would be possible. remaining hotspot is clear, 
                # and light pms require that no vm move in (so dest is safe)
                for i in range(len(move[1].load)):
                    new_load = move[0].load[i] * move[0].pm.total[i]
                    new_load /= move[1].total[i]
                    move[1].load[i] += new_load
                move[1].vms.append(move[0])
                # recheck, can be removed if no bug
                (after_type, after_penalty) = \
                    self.compute_penalty(move[1].load)
                if after_type == HOT:
                    raise Exception("generating new hotspot...!!!")
        if not self.alloc_sleep_pm:
            # try to close a pm
            for pm in self.coldspots:
                # not handle the pm that just has vm in
                if len(pm.vms) == 1:
                    move = self.close_one_vm_pm(pm)
                    if move is not None:
                        # move it!
                        self.migration_list[move[0].fqdn] = move[1].fqdn
                        # update the dest info
                        # vm would not be consider anyway
                        for i in range(len(move[1].load)):
                            new_load = move[0].load[i] * move[0].pm.total[i]
                            new_load /= move[1].total[i]
                            move[1].load[i] += new_load
                        move[1].vms.append(move[0])
                        # update the src pm to make it sleep,
                        # preventing it to be select as a dest later
                        for i in range(len(pm.load)):
                            pm.load[i] -= move[0].load[i]
                        pm.vms = []
                        # recheck, can be removed if no bug
                        (after_type, after_penalty) = \
                            self.compute_penalty(move[1].load)
                        if type == HOT:
                            raise Exception("generating new hotspot...!!!")
        return self.migration_list

    ###################################
    # the predicted value and layout will reset to empty or none
    ###################################
    def reset(self):
        # hot pms
        self.hotspots = []
        # pms with only one vm
        self.coldspots = []
        # predicted load & layout
        self.predicted_vms = []
        self.predicted_pms = []
        self.predicted_sys = None
        self.layout = {}
        # return value
        self.migration_list = {}
        # two dicts
        self.pmdict = {}
        self.vmdict = {}

    ###################################
    # set up attributes in self.predicted_pms
    ###################################
    def setup(self):
        # compute pm loads and find and sort hot pms
        # this algorithm would rely on only:
        # pm: vms, overload, load and total
        # vm: pm.total, load and ram
        self.generate_dicts_not_copy()
        for pm in self.predicted_pms:
            # we would fill the following and use them
            pm.load = []
            pm.overload = 0.0
            pm.total = []
            # cpu
            pm.load.append(pm.cpu / pm.cap_cpu)
            pm.total.append(pm.cap_cpu)
            # ram
            pm.load.append(pm.ram / pm.cap_ram)
            pm.total.append(pm.cap_ram)
            # net_rx
            pm.load.append(pm.net_rx / pm.cap_net)
            pm.total.append(pm.cap_net)
            # net_tx
            pm.load.append(pm.net_tx / pm.cap_net)
            pm.total.append(pm.cap_net)
            for l in pm.load:
                if l > self.hot_threshold:
                    pm.overload += (l - self.hot_threshold) ** 2
            if pm.overload > 0:
                self.hotspots.append(pm)
            elif len(pm.vms) == 0:
                # sleeping pm
                pm.load = [0, 0, 0, 0]
            elif len(pm.vms) == 1:
                # not overload and only domU
                self.coldspots.append(pm)
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
        # sort hotspots by overload, reverse
        self.hotspots.sort(key=lambda x: x.overload, reverse=True)
        # sort coldspots by ram
        self.coldspots.sort(key=lambda x: x.vms[0].ram)

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
    # generate mig to solve hotspot
    ###################################
    def remove_hotspot(self, pm):
        # candidate_move is used to store all the valid move
        candidate_move = []
        # see all the vms on this hotspot
        for vm in pm.vms:
            # check the load after move this vm out
            load_after_out = []
            for i in range(len(pm.load)):
                load_after_out.append(pm.load[i] - vm.load[i])
            (vm_out_type, vm_out_penalty) = \
                self.compute_penalty(load_after_out)
            if vm_out_type != HOT:
                # no COOL or WARM for a PM who is hotspot 
                vm_out_type = NOT_HOT
            for dest in self.predicted_pms:
                # skip hot, not enough freeram and itself
                if dest.overload > 0.0:
                    continue
                if vm.pm.fqdn == dest.fqdn:
                    continue
                # pm_orig_penalty would be used to compute delta
                (pm_orig_type, pm_orig_penalty) = \
                    self.compute_penalty(dest.load)
                # check load after move to this dest
                load_after_in = []
                for i in range(len(dest.load)):
                    l = vm.load[i] * vm.pm.total[i]
                    l /= dest.total[i]
                    l = dest.load[i] + l
                    load_after_in.append(l)
                (pm_in_type, pm_in_penalty) = \
                    self.compute_penalty(load_after_in)
                # no new hotspot
                if pm_in_type == HOT:
                    continue
                # to here, wo could promise
                # the dest would not be hotspot after migration
                delta_pm_penalty = pm_in_penalty - pm_orig_penalty
                if vm_out_type == HOT:
                    # type is most important
                    # dest type (COOL or WARM) secondly
                    # the third value is overload
                    # the fourth is delta
                    candidate_move.append((vm_out_type, pm_in_type,
                                           vm_out_penalty, delta_pm_penalty,
                                           vm, dest))
                else:
                    # the third value is overload
                    # the fourth considers total change of distance
                    candidate_move.append((vm_out_type, pm_in_type, 0,
                                           delta_pm_penalty + vm_out_penalty,
                                           vm, dest))
        if len(candidate_move) != 0:
            best_move = min(candidate_move)
            return (best_move[4], best_move[5])
        else:
            return None

    ###################################
    # close the pm by mig the only vm
    ###################################
    def close_one_vm_pm(self, pm):
        candidate_dest = []
        vm = pm.vms[0]
        for dest in self.predicted_pms:
            # skip hot, not enough resources and itself
            if dest.overload > 0.0:
                continue
            if pm.fqdn == dest.fqdn:
                continue
            # pm_orig_penalty would be used to compute delta
            (pm_orig_type, pm_orig_penalty) = \
                self.compute_penalty(dest.load)
            # accept only cool ones
            if pm_orig_type != COOL:
                continue
            # check load after move to this dest
            load_after_in = []
            for i in range(len(dest.load)):
                l = vm.load[i] * vm.pm.total[i]
                l /= dest.total[i]
                l = dest.load[i] + l
                load_after_in.append(l)
            (pm_in_type, pm_in_penalty) = \
                self.compute_penalty(load_after_in)
            # accept only cool ones
            if pm_in_type != COOL:
                continue
            # to here, wo could promise
            # the dest would be still cool after migration
            delta_pm_penalty = pm_in_penalty - pm_orig_penalty
            candidate_dest.append((delta_pm_penalty, dest))
        if len(candidate_dest) != 0:
            best_move = min(candidate_dest)
            return (vm, best_move[1])
        else:
            return None

    ###################################
    # compute distance
    ###################################
    def compute_penalty(self, load):
        # base on the assumption that dimensions of sleeping PM's load
        # are all zero
        is_empty = True
        for l in load:
            if l != 0:
                is_empty = False
        if is_empty:
            return (SLEEP, 0)
        # compute overload when hot
        is_hot = False
        overload = 0.0
        for l in load:
            if l > self.hot_threshold:
                is_hot = True
                overload += (l - self.hot_threshold) ** 2
        if is_hot:
            return (HOT, overload)
        # distance otherwise
        # we want to keep the dest COOL
        is_cool = True
        for l in load:
            if l > self.warm_threshold:
                is_cool = False
                break
        distance = 0.0
        for l in load:
            distance += (l - self.warm_threshold) ** 2
        if not is_cool:
            return (WARM, distance)
        else:
            return (COOL, distance)
