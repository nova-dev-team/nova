package nova.master.daemons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import nova.common.util.PerfData;
import nova.common.util.RRDTools;
import nova.common.util.SimpleDaemon;
import nova.master.api.messages.MasterMigrateVnodeMessage;
import nova.master.handler.MasterMigrateVnodeHandler;
import nova.master.models.Pnode;
import nova.master.models.Vnode;

/**
 * a simple vm scheduling algorithm. load balancing.
 * 
 * @author Tianyu Chen
 */
public class LoadBalancingDaemon extends SimpleDaemon {

    /**
     * Logger
     */
    Logger logger = Logger.getLogger(AutoManagerDaemon.class);

    /**
     * the spot value threshold. value that is larger will be considered a
     * "hot spot".
     */
    double spotThreshold;
    /**
     * the energy saving threshold. if the spot values of all pnodes are below
     * this threshold, the entire system will enter a "power-saving" state.
     */
    double powerSavingThreshold;

    public LoadBalancingDaemon(double setSpotThreshold,
            double setPowerSavingThreshold) {
        // wait 180s (3 min) for next round
        super(180000);
        this.spotThreshold = setSpotThreshold;
        this.powerSavingThreshold = setPowerSavingThreshold;
    }

    /**
     * calculate spot value using performance data.
     * 
     * @param cpu
     * @param mem
     * @param netIn
     * @param netOut
     * @return
     */
    protected double calculateSpotValue(PerfData perf) {
        return (1.0 / (1.0 - perf.getCpuLoad()))
                * (1.0 / (1.0 - perf.getMemLoad()))
                * ((1.0 / (1.0 - perf.getNetInLoad()))
                        + (1.0 / (1.0 - perf.getNetOutLoad())));
    }

    /**
     * calculate shift value for a virtual machine
     * 
     * @param cpu
     * @param mem
     * @param netIn
     * @param netOut
     * @param memSize
     * @return
     */
    protected double calculateShiftValue(PerfData perf) {
        // !!! may be some issues with the memory size
        // use the highest single value and memory size to determine
        double highest = Collections.max(Arrays.asList(perf.getCpuLoad(),
                perf.getMemLoad(), perf.getNetInLoad(), perf.getNetOutLoad()));
        return highest / perf.getMemSize();
    }

    /**
     * select hot spot physical machine
     * 
     * @param pnodes
     * @return
     */
    protected Pnode selectPnodeHotspot(List<Pnode> pnodes) {
        // collecting CPU / MEM / NETWORK IO performance data
        Pnode hotspot = null;
        double hotspotSpotValue = -1.0;
        for (Pnode pnd : pnodes) {
            logger.info(
                    String.format("Pnode %d says aye! ", (int) pnd.getId()));

            int pndId = (int) pnd.getId();
            PerfData perf = new PerfData(
                    RRDTools.getLatestPnodeMonitorInfo(pndId));

            double spotValue = calculateSpotValue(perf);
            /**
             * if the "spot value" of this node is larger than the threshold, we
             * recognize it as a "hot spot".
             */
            if (spotValue > this.spotThreshold) {
                if (hotspot == null) {
                    hotspot = pnd;
                    hotspotSpotValue = spotValue;
                } else {
                    if (spotValue > hotspotSpotValue) {
                        hotspot = pnd;
                        hotspotSpotValue = spotValue;
                    }
                }
            }
        }

        // return null if no hot spot is detected
        return hotspot;
    }

    /**
     * select the most "influential" virtual machine from the hot spot
     * 
     * @param hotspot
     * @return
     */
    protected Vnode selectVnodeFromHotspot(Pnode hotspot) {
        // debug info
        int vmCnt = hotspot.getCurrentVMNum();
        logger.info(String.format(
                "Well, pnode %d is a hot spot. There are %d vnodes on it. ",
                (int) hotspot.getId(), vmCnt));

        List<Vnode> vnodes = hotspot.getVnodes();
        if (vnodes.isEmpty()) {
            // throw exception if the hot spot is not due to high payload on
            // virtual machines
            throw new NoVnodeOnHotspotException();
        }

        Vnode migrant = null;
        double maxShiftValue = -1.0;
        for (Vnode vnd : vnodes) {
            PerfData perf = new PerfData(
                    RRDTools.getLatestVnodeMonitorInfo(vnd.getUuid()));

            double shiftValue = calculateShiftValue(perf);
            // choose the virtual machine with the largest shift value
            if (shiftValue > maxShiftValue) {
                migrant = vnd;
                maxShiftValue = shiftValue;
            }
        }

        return migrant;
    }

    /**
     * selecting virtual machine migration deestination.
     * 
     * @param migrant
     * @param pnodes
     * @return
     */
    protected Pnode selectPnodeDestination(Vnode migrant, List<Pnode> pnodes) {
        /**
         * three qualifications of destination physical machine:
         * 
         * 1. destination cpu < 1 - CPU / N 2. destination free mem >= memory
         * required by migrant 3. destination free net i/o >= net i/o required
         * by migrant
         */

        Pnode destination = null;
        double minSpotValue = -1.0;
        PerfData migrantPerf = new PerfData(
                RRDTools.getLatestVnodeMonitorInfo(migrant.getUuid()));

        for (Pnode dest : pnodes) {
            PerfData destPerf = new PerfData(
                    RRDTools.getLatestPnodeMonitorInfo((int) dest.getId()));

            boolean cpuAvailable = destPerf.getCpuLoad() < (1
                    - migrantPerf.getCpuLoad() / dest.getCurrentVMNum());
            boolean memAvailable = destPerf.getFreeMemSize() >= migrantPerf
                    .getMemSize();
            boolean netIoAvailable = ((destPerf.getBandWidth()
                    - destPerf.getNetIn()) >= migrantPerf.getBandWidth())
                    && ((destPerf.getBandWidth()
                            - destPerf.getNetOut()) >= migrantPerf
                                    .getBandWidth());
            if (cpuAvailable && memAvailable && netIoAvailable) {
                if (destination == null) {
                    destination = dest;
                    minSpotValue = calculateSpotValue(destPerf);
                } else {
                    double thisPnodeSpotValue = calculateSpotValue(destPerf);
                    if (thisPnodeSpotValue < minSpotValue) {
                        destination = dest;
                        minSpotValue = thisPnodeSpotValue;
                    }
                }
            }
        }

        // return null if no available destinations
        return destination;
    }

    /**
     * method to decide whether it should enter the "power saving mode" .
     * 
     * @param pnodes
     * @return
     */
    protected boolean enterPowerSavingMode(List<Pnode> pnodes) {
        boolean ret = true;
        for (Pnode pnd : pnodes) {
            PerfData perf = new PerfData(
                    RRDTools.getLatestPnodeMonitorInfo((int) pnd.getId()));

            if (calculateSpotValue(perf) >= powerSavingThreshold) {
                ret = false;
                break;
            }
        }

        return ret;
    }

    protected void powerSavingScheduling(List<Pnode> pnodes) {
        /**
         * TBD
         */
        // 1. find out the max of numbers of vnodes that can be run on a single
        // pnode
        // VNODE_NUM = MIN(CPU_CAPACITY, MEM_CAPACITY, NET-IO_CAPACITY)
        List<Vnode> vnodes = Vnode.all();
        if (vnodes.isEmpty()) {
            logger.info("No virtual machine running! ");
            return;
        }
        List<Double> cpu = new ArrayList<Double>();
        List<Double> mem = new ArrayList<Double>();

        for (Vnode vnd : vnodes) {
            PerfData perf = new PerfData(
                    RRDTools.getLatestVnodeMonitorInfo(vnd.getUuid()));
            cpu.add(perf.getCpuLoad());
            mem.add(perf.getUsedMemSize());
        }
        // !!!TBD!!! get ncpus here!!!
        int ncpu = 0;
        // int cpuCapacity = (int) Math.floor((double) ncpu / max
        // 2. find out how many pnodes are required to operate these vnodes
        // 3. find out the pnodes with the most vnodes already running and make
        // them the destinations of migration
        // 4. migrate

    }

    /**
     * work one round.
     */
    @Override
    protected void workOneRound() {
        List<Pnode> pnodes = Pnode.all();
        // 1. find the hot spot of physical machines
        Pnode hotspot = this.selectPnodeHotspot(pnodes);

        if (hotspot == null) {
            logger.info("No hotspot detected. Entering energy saver...");
            /**
             * TBD
             */
            if (enterPowerSavingMode(pnodes)) {
                powerSavingScheduling(pnodes);
            }
        } else {
            logger.info("Hotspot detected! ");
            // 2. choose the most influential vm on the physical node
            Vnode migrant = this.selectVnodeFromHotspot(hotspot);
            if (migrant == null) {
                throw new NullPointerException();
            }

            // 3. find the best destination
            Pnode destination = this.selectPnodeDestination(migrant, pnodes);
            // better luck next time! :P
            if (destination == null) {
                logger.info("Ah... no available destination found! ");
                return;
            }

            // 4. migrate
            new MasterMigrateVnodeHandler().handleMessage(
                    new MasterMigrateVnodeMessage(migrant.getId(),
                            hotspot.getId(), destination.getId()),
                    null, null, null);
        }
    }
}
