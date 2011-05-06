import math
import plugin
import datetime, time
from usher.utils import notify
lg = notify.get_notify()

class Algorithm(object):

    def __init__(self,args):
        self.max_swap = args['max_swap']
        self.max_cpu = args['max_cpu']
        self.max_net = args['max_net']
        self.max_mem = args['max_mem']
        self.layout = {}
        self.predicted_vms = []
        self.predicted_pms = []
        self.predicted_sys = []

    def __str__(self):
        return "blackbox"

    def schedule(self,layout,predicted_vms,predicted_pms,predicted_sys):
        """Return the new schedule.  This must be implemented by all
        Schedulers."""
        # initialize
        self.layout = layout
        self.predicted_vms = predicted_vms
        self.predicted_pms = predicted_pms
        self.predicted_sys = predicted_sys

        pmdict = {}
        vmdict = {}
        for vm in self.predicted_vms:
            vmdict[vm.fqdn] = vm
        for pm in self.predicted_pms:
            pmdict[pm.fqdn] = pm
        for vmf, pmf in self.layout.items():
            pmdict[pmf].vms.append(vmdict[vmf])

        self.moves = []
        # compute the result here
        self.make_moves()

        return self.layout

    def make_moves(self):
        """Move VMs"""
        self.layout = {}
        self.moves = self.moves_best_list()
        if not self.moves:
            return

        self.layout[self.moves[0][0].fqdn] = self.moves[0][1].fqdn

    def calc_pm_cost(self,pm):
        if pm.cpu_rate == 1.0 or pm.net_tx_rate == 1.0 or pm.net_rx_rate == 1.0 or pm.ram_rate == 1.0:
            return 1e100
        if (1/(1-pm.cpu_rate))*(1/(1-pm.net_rx_rate))*(1/(1-pm.net_tx_rate))*(1/(1-pm.ram_rate)) < 0:
            return 1e100
        return (1/(1-pm.cpu_rate))*(1/(1-pm.net_rx_rate))*(1/(1-pm.net_tx_rate))*(1/(1-pm.ram_rate))

    def calc_vm_cost(self, pm):
        vmcostlist = []

        for vm in pm.vms:
            if vm.cpu/pm.cap_cpu == 1.0 or vm.net_tx/pm.cap_net == 1.0 or vm.net_rx/pm.cap_net == 1.0 or vm.ram/pm.cap_ram == 1.0:
                cost = 1e100
            elif (1/(1-vm.cpu/pm.cap_cpu))*(1/(1-vm.net_tx/pm.cap_net))*(1/(1-vm.net_rx/pm.cap_net))*(1/(1-vm.ram/pm.cap_ram)) < 0:
                cost = 1e100
            else:
                cost = (1/(1-vm.cpu/pm.cap_cpu))*(1/(1-vm.net_tx/pm.cap_net))*(1/(1-vm.net_rx/pm.cap_net))*(1/(1-vm.ram/pm.cap_ram))

            if vm.ram == 0:
                cost = 1e100
            else:
                cost = cost/vm.ram

            vmcostlist.append((cost,vm))
        # sort and reverse
        vmcostlist.sort()
        vmcostlist.reverse()
        return vmcostlist

    def moves_best_list(self):
        moves = []
        hot_pms = []
        pmcostlist = []

        # detect hotspots
        for pm in self.predicted_pms:
            # check with threshold
            if pm.cpu_rate >= self.max_cpu:
                hot_pms.append((self.calc_pm_cost(pm),pm))
                continue
            if pm.ram_rate >= self.max_mem:
                hot_pms.append((self.calc_pm_cost(pm),pm))
                continue
            if pm.net_tx_rate >= self.max_net:
                hot_pms.append((self.calc_pm_cost(pm),pm))
                continue
            if pm.net_rx_rate >= self.max_net:
                hot_pms.append((self.calc_pm_cost(pm),pm))
                continue

        # sort hotspots
        hot_pms.sort()
        # compute all pms cost for vm to select to migrate
        for pm in self.predicted_pms:
            pmcostlist.append((self.calc_pm_cost(pm),pm))
        pmcostlist.sort()

        # no hotspots
        if not hot_pms:
            lg.notify(notify.DEBUG,"BLACKBOX not hotspots")
            return moves

        # get only the hottest one
        hpm = hot_pms[-1][1]
        lg.notify(notify.DEBUG,"BLACKBOX Hotspots:%s"%hpm.fqdn)

        # choose the hottest vm
        hpm_vmcostlist = []
        hpm_vmcostlist = self.calc_vm_cost(hpm)

        for vals in hpm_vmcostlist:
            # list of (vm.cost,vm)
            vm = vals[1]

            # choose pm to migrate
            for vals in pmcostlist:
                pm = vals[1]
                cpu = (pm.cpu + vm.cpu) / pm.cap_cpu
                net_tx = (pm.net_tx + vm.net_tx) / pm.cap_net
                net_rx = (pm.net_rx + vm.net_rx) / pm.cap_net

                # cpu not hot
                if cpu >= self.max_cpu:
                    continue
                # network not hot
                elif net_tx >= self.max_net:
                    continue
                elif net_rx >= self.max_net:
                    continue
                else:
                    moves.append((vm,pm))
                    self.move_count = 1
                    return moves
        return moves
