from heapq import heappush, heappop
from sets import Set
import operator

class Algorithm(object):
    ###################################
    # init
    ###################################
    def __init__(self, args):
        self.hot_threshold = args["hot"]

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
        # setup pm and vm load
        self.setup()
        # search
        result = self.search()
        # return
        for (vm, pm) in result:
            if vm.pm.fqdn != pm.fqdn:
                self.migration_list[vm.fqdn] = pm.fqdn
        return self.migration_list

    ###################################
    # the predicted value and layout will reset to empty or none
    ###################################
    def reset(self):
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
        # pm: vms and total
        # vm: pm.total and load
        self.generate_dicts_not_copy()
        for pm in self.predicted_pms:
            # we would fill the following and use them
            pm.total = []
            # cpu
            pm.total.append(pm.cap_cpu)
            # ram
            pm.total.append(pm.cap_ram)
            # net_rx
            pm.total.append(pm.cap_net)
            # net_tx
            pm.total.append(pm.cap_net)
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

    def search(self):
        # heuristic
        def h(state):
            # remains for used pm
            remains = [0] * 4
            pm_set = Set()
            for (vm, pm) in state:
                pm_set.add(pm.fqdn)
                remains = map(operator.sub, remains, vm.load)
            for fqdn in pm_set:
                pm_capacity = \
                    [t * self.hot_threshold for t in self.pmdict[fqdn].total]
                remains = map(operator.add, remains, pm_capacity)
            # should all >= 0
            if min(remains) < 0:
                raise Exception("load is too high!")
            # return value
            ret = 0
            pm_capacity = [t * self.hot_threshold for t in self.predicted_pms[0].total]
            for remaining_vm in self.predicted_vms[len(state):]:
                # subtract the remaining
                remains = map(operator.sub, remains, remaining_vm.load)
                # add new ones if not enough
                while min(remains) < 0.0:
                    ret += 1
                    remains = map(operator.add, remains, pm_capacity)
            return ret
        # cost
        def g(state):
            return len(state)
        # evaluation
        def f(state):
            return g(state) + h(state)
        # insert into fringe
        def insert(state, fringe):
            heappush(fringe, (f(state), state))
        # search begins
        # fringe
        fringe = []
        # init
        insert([], fringe)
        while True:
            if len(fringe) == 0:
                return None
            # get one
            (f_state, state) = heappop(fringe)
            # goal test
            if len(state) == len(self.predicted_vms):
                return state
            # next vm for the state
            next_vm = self.predicted_vms[len(state)]
            # next pm and check the validation
            for next_pm in self.predicted_pms:
                is_valid = True
                succ = state[:]
                succ.append((next_vm, next_pm))
                remains = {}
                for pm in self.predicted_pms:
                    remains[pm] = [t * self.hot_threshold for t in pm.total]
                for (vm, pm) in succ:
                    remains[pm] = map(operator.sub, remains[pm], vm.load)
                for pm in remains:
                    if min(remains[pm]) < 0.0:
                        is_valid = False
                if is_valid:
                    insert(succ, fringe)
